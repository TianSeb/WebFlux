package tianseb.example.springwebflux.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tianseb.example.springwebflux.models.documents.Producto;
import tianseb.example.springwebflux.services.ProductoService;

import java.util.Objects;

@Component
@Slf4j
public record ProductoHandler(ProductoService productoService, Validator validator) {

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse.ok()
                .body(productoService.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request) {
        var id = request.pathVariable("id");
        return productoService.findById(id)
                .flatMap(producto -> ServerResponse.ok()
                        .bodyValue(producto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        return request.bodyToMono(Producto.class)
                .flatMap(p -> {
                    Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
                    validator.validate(p, errors);
                    return errors.hasErrors() ? mapValidationErrors(errors) :
                            productoService.save(p)
                                    .flatMap(pdb -> ServerResponse.ok().bodyValue(pdb));
                });
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        var producto = request.bodyToMono(Producto.class);
        var id = request.pathVariable("id");

        return productoService.findById(id)
                .zipWith(producto, this::updateProduct)
                .flatMap(p -> ServerResponse.ok()
                        .bodyValue(productoService.save(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        var id = request.pathVariable("id");
        return productoService.findById(id)
                .flatMap(p -> productoService.delete(p)
                        .then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> mapValidationErrors(Errors errors) {
        return Flux.fromIterable(errors.getFieldErrors())
                .map(fieldError -> "Campo: "
                        .concat(fieldError.getField())
                        .concat(" ")
                        .concat(Objects.requireNonNull(fieldError.getDefaultMessage())))
                .collectList()
                .flatMap(list -> ServerResponse.badRequest().bodyValue(list));
    }

    private Producto updateProduct(Producto productoDb, Producto productoRequest) {
        productoDb.setNombre(productoRequest.getNombre());
        productoDb.setPrecio(productoRequest.getPrecio());
        productoDb.setCategoria(productoDb.getCategoria());
        return productoDb;
    }
}
