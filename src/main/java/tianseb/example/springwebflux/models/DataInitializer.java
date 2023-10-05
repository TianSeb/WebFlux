package tianseb.example.springwebflux.models;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import tianseb.example.springwebflux.models.dao.ProductoDao;
import tianseb.example.springwebflux.models.documents.Producto;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final ProductoDao productoDao;

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        mongoTemplate.dropCollection("productos")
                .subscribe();

        Flux.just(
                new Producto("Manzana",252.23),
                new Producto("Naranjas",155.55),
                new Producto("Peras",211.05),
                new Producto("Zanahorias",119.55),
                new Producto("Bananas",555.23),
                new Producto("Kiwis",232.55),
                new Producto("Sandias", 124.23)
                )
                .flatMap(producto -> {
                    producto.setCreatedAt(new Date());
                    return productoDao.save(producto);
                })
                .subscribe(producto -> log.info("Insert: {}, Name: {}",
                        producto.getId(),
                        producto.getNombre()));
    }
}
