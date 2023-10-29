package tianseb.example.springwebflux.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tianseb.example.springwebflux.models.documents.Categoria;
import tianseb.example.springwebflux.models.documents.Producto;
import tianseb.example.springwebflux.services.ProductoService;

import java.time.Duration;
import java.util.Date;

@Controller
@Slf4j
public record ProductoController(ProductoService productoService) {

    @ModelAttribute("categorias")
    public Flux<Categoria> categorias() {
        return productoService.findAllCategoria();
    }

    @GetMapping({"/listar", "/"})
    public Mono<String> listar(Model model) {
        Flux<Producto> productos = productoService
                .findAll();

        productos.subscribe(Producto::getNombre);
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado Productos");
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "formulario de producto");
        model.addAttribute("boton", "crear");
        return Mono.just("form");
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarv2(@PathVariable String id, Model model) {
        return productoService.findById(id)
                .doOnNext(producto -> {
                    log.info("Producto: {}", producto.getNombre());
                    model.addAttribute("boton", "editar");
                    model.addAttribute("titulo", "Editar producto");
                    model.addAttribute("producto", producto);
                })
                .defaultIfEmpty(new Producto())
                .flatMap(producto -> {
                    if (producto.getId().isEmpty()) {
                        return Mono.error(new InterruptedException("no existe el producto"));
                    }
                    return Mono.just(producto);
                })
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+producto"));
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {
        Mono<Producto> productoMono = productoService.findById(id)
                .doOnNext(producto -> {
                    log.info("Producto: {}", producto.getNombre());
                })
                .defaultIfEmpty(new Producto());

        model.addAttribute("boton", "editar");
        model.addAttribute("titulo", "Editar producto");
        model.addAttribute("producto", productoMono);

        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Error en formulario");
            model.addAttribute("boton", "Guardar");
            return Mono.just("form");
        } else {
            Mono<Categoria> categoria = productoService.findCategoriaById(producto.getCategoria().getId());
            return categoria.flatMap(c -> {
                        if (producto.getCreatedAt() == null) {
                            producto.setCreatedAt(new Date());
                        }
                        producto.setCategoria(c);
                        return productoService.save(producto);
                    })
                    .doOnNext(p -> {
                        log.info("Producto guardado: {} ,Id: {}, Categoria: {}", p.getNombre(),
                                p.getId(), p.getCategoria());
                    })
                    .thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }
    }

    @GetMapping("eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {
        return productoService.findById(id)
                .defaultIfEmpty(new Producto())
                .flatMap(producto -> {
                    if (producto.getId().isEmpty()) {
                        return Mono.error(new InterruptedException("no existe el producto"));
                    }
                    return Mono.just(producto);
                })
                .flatMap(productoService::delete)
                .then(Mono.just(("redirect:/listar?success=producto+eliminado")))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+producto"));
    }

    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {
        Flux<Producto> productos = productoService
                .findAllConNombreUpperCase()
                .delayElements(Duration.ofSeconds(1));

        productos.subscribe(producto -> log.info(producto.getNombre()));
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
        model.addAttribute("titulo", "Listado Productos");
        return "listar";
    }

    @GetMapping("/listar-full")
    public String listarFull(Model model) {
        Flux<Producto> productos = productoService
                .findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado Productos");
        return "listar";
    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {
        Flux<Producto> productos = productoService
                .findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado Productos");
        return "listar-chunked";
    }
}
