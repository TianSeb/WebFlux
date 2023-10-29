package tianseb.example.springwebflux.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import tianseb.example.springwebflux.models.documents.Categoria;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {
}
