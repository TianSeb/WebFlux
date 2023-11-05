package tianseb.example.springwebflux;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import tianseb.example.springwebflux.models.documents.Producto;
import tianseb.example.springwebflux.services.ProductoService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringWebfluxApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductoService productoService;


    @Test
    void listarTest() {
        client.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Producto.class)
                .consumeWith(response -> {
                    List<Producto> productos = response.getResponseBody();
                    assertThat(productos).isNotNull();
                });
    }

    @Test
    void verProductoById() {
        var NOMBRE_PRODUCTO = "Manzana";
        var producto = productoService.findByNombre(NOMBRE_PRODUCTO).block();

        client.get()
                .uri("/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo(NOMBRE_PRODUCTO);
    }

    @Test
    void crear() {
        var categoria = Objects.requireNonNull(productoService.findAllCategoria()
                        .collectList()
                        .block())
                        .getFirst();
        var producto = new Producto("Nombre", 222.2, categoria);

        client.post().uri("/")
                .bodyValue(producto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    var productoCreado = response.getResponseBody();
                    assertThat(productoCreado.getNombre()).isEqualTo("Nombre");
                    assertThat(productoCreado.getCategoria().getNombre())
                            .isEqualTo(categoria.getNombre());
                });
    }

    @Test
    void editar() {
        var NOMBRE_PRODUCTO = "Manzana";
        var producto = productoService.findByNombre(NOMBRE_PRODUCTO).block();
        var productoEditado = new Producto("Rogelio", 102.12);

        client.put()
                .uri("/{id}", Collections.singletonMap("id", producto.getId()))
                .bodyValue(productoEditado)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Producto.class)
                .consumeWith(response -> {
                   var updatedProd = response.getResponseBody();
                   assertThat(updatedProd.getNombre()).isEqualTo("Rogelio");
                });
    }

    @Test
    void eliminar() {
        var NOMBRE_PRODUCTO = "Manzana";
        var producto = productoService.findByNombre(NOMBRE_PRODUCTO).block();

        client.delete()
                .uri("/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();

        client.delete()
                .uri("/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .isEmpty();

    }
}
