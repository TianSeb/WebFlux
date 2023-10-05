package tianseb.example.springwebflux.models.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "productos")
@NoArgsConstructor
@Data
public class Producto {

    @Id
    private String id;
    private String nombre;
    private Double precio;
    private Date createdAt;

    public Producto(String nombre, Double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }
}
