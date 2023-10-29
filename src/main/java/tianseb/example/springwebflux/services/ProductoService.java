package tianseb.example.springwebflux.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tianseb.example.springwebflux.models.documents.Categoria;
import tianseb.example.springwebflux.models.documents.Producto;

public interface ProductoService {

    Flux<Producto> findAll();
    Flux<Producto> findAllConNombreUpperCase();
    Flux<Producto> findAllConNombreUpperCaseRepeat();
    Mono<Producto> findById(String id);
    Mono<Producto> save(Producto producto);
    Mono<Void> delete(Producto producto);
    Flux<Categoria> findAllCategoria();
    Mono<Categoria> findCategoriaById(String id);
    Mono<Categoria> saveCategoria(Categoria categoria);


}
