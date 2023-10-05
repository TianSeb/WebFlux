package tianseb.example.springwebflux.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tianseb.example.springwebflux.models.dao.ProductoDao;
import tianseb.example.springwebflux.models.documents.Producto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/productos")
@Slf4j
public class ProductoRestController {

    private final ProductoDao dao;

    @GetMapping
    public Flux<Producto> index() {
        return dao.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                })
                .doOnNext(producto -> log.info(producto.getNombre()));
    }

    @GetMapping("/{id}")
    public Mono<Producto> show(@PathVariable String id) {
        return dao.findAll()
                .filter(prod -> id.equals(prod.getId()))
                .next()
                .defaultIfEmpty(new Producto());
    }
}
