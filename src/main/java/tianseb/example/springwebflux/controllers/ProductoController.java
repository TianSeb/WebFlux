package tianseb.example.springwebflux.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import tianseb.example.springwebflux.models.dao.ProductoDao;
import tianseb.example.springwebflux.models.documents.Producto;

import java.time.Duration;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoDao dao;

    @GetMapping({"/listar", "/"})
    public String listar(Model model) {
        Flux<Producto> productos = dao.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                });

        productos.subscribe(Producto::getNombre);
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado Productos");
        return "listar";
    }

    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {
        Flux<Producto> productos = dao.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                })
                .delayElements(Duration.ofSeconds(1));

        productos.subscribe(producto -> log.info(producto.getNombre()));
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
        model.addAttribute("titulo", "Listado Productos");
        return "listar";
    }

    @GetMapping("/listar-full")
    public String listarFull(Model model) {
        Flux<Producto> productos = dao.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                }).repeat(5000);

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado Productos");
        return "listar";
    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {
        Flux<Producto> productos = dao.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                }).repeat(5000);

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado Productos");
        return "listar-chunked";
    }
}
