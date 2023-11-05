package tianseb.example.springwebflux.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import tianseb.example.springwebflux.models.documents.Producto;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {

    Mono<Producto> findByNombre(String nombre);
}
