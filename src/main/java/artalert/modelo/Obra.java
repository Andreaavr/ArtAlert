package artalert.modelo;

import java.util.Objects;

/**
 * Obra de arte tal como la devuelve un museo.
 * Es inmutable: si cambia algo (por ejemplo, sale a sala), la fuente devuelve una Obra nueva.
 *
 * @param id      identificador de la obra dentro de su museo
 * @param titulo  título de la obra
 * @param artista nombre del artista tal como lo da el museo
 * @param museo   nombre del museo de origen
 * @param enSala  true si la obra está expuesta ahora mismo
 */
public record Obra(String id, String titulo, String artista, String museo, boolean enSala) {

    public Obra {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(museo, "museo");
        titulo = (titulo == null || titulo.isBlank()) ? "Sin título" : titulo;
        artista = (artista == null || artista.isBlank()) ? "Desconocido" : artista;
    }

    /** Clave única entre todos los museos (dos museos pueden repetir el mismo id). */
    public String clave() {
        return museo + ":" + id;
    }
}
