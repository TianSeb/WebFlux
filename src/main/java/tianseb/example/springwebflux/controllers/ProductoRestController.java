package tianseb.example.springwebflux.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tianseb.example.springwebflux.models.documents.Producto;
import tianseb.example.springwebflux.services.ProductoService;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/productos")
@Slf4j
public record ProductoRestController(ProductoService productoService) {

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> index() {
        return Mono.just(ResponseEntity.ok()
                .body(productoService.findAll()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> show(@PathVariable String id) {
        return productoService.findById(id)
                .map(producto -> ResponseEntity.ok().body(producto))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> crear(
            @Valid @RequestBody Mono<Producto> monoProducto) {
        Map<String, Object> respuesta = new HashMap<>();

        return monoProducto.flatMap(producto -> {
            return productoService.save(producto)
                    .map(p -> {
                        respuesta.put("producto", p);
                        return ResponseEntity.created(
                                URI.create("/api/productos/".concat(p.getId())))
                                .body(respuesta);
                    });
        })
            .onErrorResume(throwable -> {
                return Mono.just(throwable).cast(WebExchangeBindException.class)
                        .flatMap(e -> Mono.just(e.getFieldErrors()))
                        .flatMapMany(Flux::fromIterable)
                        .map(fieldError ->
                                "campo: ".concat(fieldError.getField())
                                        .concat(" ")
                                        .concat(Objects.requireNonNull(fieldError.getDefaultMessage())))
                        .collectList()
                        .flatMap(list -> {
                            respuesta.put("errors", list);
                            respuesta.put("timestamp", new Date());
                            return Mono.just(ResponseEntity.badRequest().body(respuesta));
                        });
            });

    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> editar(@PathVariable String id,
                                                 @RequestBody Producto producto) {
        return productoService.findById(id)
                .flatMap(p -> {
                    p.setNombre(producto.getNombre());
                    p.setPrecio(producto.getPrecio());
                    p.setCategoria(producto.getCategoria());
                    return productoService.save(p);
                })
                .map(p -> ResponseEntity.ok().body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {
        return productoService.findById(id)
                .flatMap(producto -> {
                    return productoService.delete(producto)
                            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
                })
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
