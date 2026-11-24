package artalert.modelo;

import java.util.Objects;

/**
 * Obra de arte tal como la devuelve un museo.
 * Es inmutable: si cambia algo (por ejemplo, sale a sala), la fuente devuelve una Obra nueva.
 *
 * @param id        identificador de la obra dentro de su museo
 * @param titulo    título de la obra
 * @param artista   nombre del artista tal como lo da el museo
 * @param museo     nombre del museo de origen
 * @param enSala    true si la obra está expuesta ahora mismo
 * @param imagenUrl dirección de una imagen de la obra, o null si el museo no la tiene
 */
public record Obra(String id, String titulo, String artista, String museo, boolean enSala, String imagenUrl) {

    public Obra {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(museo, "museo");
        titulo = (titulo == null || titulo.isBlank()) ? "Sin título" : titulo;
        artista = (artista == null || artista.isBlank()) ? "Desconocido" : artista;
        imagenUrl = (imagenUrl == null || imagenUrl.isBlank()) ? null : imagenUrl;
    }

    /** Obra sin imagen. */
    public Obra(String id, String titulo, String artista, String museo, boolean enSala) {
        this(id, titulo, artista, museo, enSala, null);
    }

    /** Clave única entre todos los museos (dos museos pueden repetir el mismo id). */
    public String clave() {
        return museo + ":" + id;
    }

    public boolean tieneImagen() {
        return imagenUrl != null;
    }

    /** La misma obra con el estado de sala cambiado. */
    public Obra conEnSala(boolean nuevoEnSala) {
        return new Obra(id, titulo, artista, museo, nuevoEnSala, imagenUrl);
    }
}
