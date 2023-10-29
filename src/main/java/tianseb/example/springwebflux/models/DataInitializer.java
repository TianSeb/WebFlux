package tianseb.example.springwebflux.models;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import tianseb.example.springwebflux.models.documents.Categoria;
import tianseb.example.springwebflux.models.documents.Producto;
import tianseb.example.springwebflux.services.ProductoService;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final ProductoService productoService;
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        mongoTemplate.dropCollection("productos")
                .subscribe();
        mongoTemplate.dropCollection("categorias")
                .subscribe();

        Categoria frutas = new Categoria("Frutas");
        Categoria verduras = new Categoria("Verduras");
        Categoria especiales = new Categoria("Especiales");

        Flux.just(frutas, verduras, especiales)
                .flatMap(productoService::saveCategoria)
                .doOnNext(categoria -> {
                    log.info("Category: {} saved", categoria.getNombre());
                })
                .thenMany(Flux.just(
                                new Producto("Manzana", 252.23, frutas),
                                new Producto("Naranjas", 155.55, frutas),
                                new Producto("Peras", 211.05, frutas),
                                new Producto("Zanahorias", 119.55, verduras),
                                new Producto("Bananas", 555.23, frutas),
                                new Producto("Kiwis", 232.55, frutas),
                                new Producto("Lechuga", 124.23, verduras),
                                new Producto("Tomate", 124.23, verduras),
                                new Producto("Hongos Magicos", 124.23, especiales)
                        )
                        .flatMap(producto -> {
                            producto.setCreatedAt(new Date());
                            return productoService.save(producto);
                        }))
                .subscribe(producto -> log.info("Insert: {}, Name: {}",
                        producto.getId(),
                        producto.getNombre()));
    }
}
