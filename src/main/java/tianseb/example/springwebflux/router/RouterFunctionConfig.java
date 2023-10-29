package tianseb.example.springwebflux.router;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import tianseb.example.springwebflux.handler.ProductoHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterFunctionConfig {

    private final ProductoHandler productoHandler;

    @Value("")
    @Bean
    public RouterFunction<ServerResponse> routes(){
        return route(GET("/"), productoHandler::listar)
                .andRoute(GET("/{id}"), productoHandler::ver)
                .andRoute(POST("/"), productoHandler::crear)
                .andRoute(PUT("/{id}"), productoHandler::editar)
                .andRoute(DELETE("/{id}"), productoHandler::delete);
    }
}
