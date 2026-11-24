package artalert.artistas;

import artalert.fuentes.FuenteObras;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Registro de los artistas que se siguen, uno por cada pareja artista + museo.
 *
 * <p>Garantiza que "Monet en Chicago" sea siempre el mismo objeto: si Ana y Luis lo siguen,
 * los dos quedan suscritos al mismo sujeto y reciben los mismos avisos.
 */
public class CatalogoArtistas {

    private final Map<String, Artista> artistas = new LinkedHashMap<>();
    private final ComparadorObras comparador;

    public CatalogoArtistas(ComparadorObras comparador) {
        this.comparador = Objects.requireNonNull(comparador, "comparador");
    }

    /** Devuelve el artista de ese museo, creándolo la primera vez que se pide. */
    public Artista obtener(String nombreArtista, FuenteObras fuente) {
        Objects.requireNonNull(fuente, "fuente");
        String clave = fuente.getNombreMuseo() + "|" + normalizar(nombreArtista);
        return artistas.computeIfAbsent(clave, k -> new Artista(nombreArtista, fuente, comparador));
    }

    /** Todos los artistas registrados (copia de solo lectura). */
    public List<Artista> getArtistas() {
        return List.copyOf(artistas.values());
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim();
    }
}