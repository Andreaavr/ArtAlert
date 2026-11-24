package artalert.fuentes;

import artalert.modelo.Obra;
import org.json.JSONObject;

/**
 * Fuente del Art Institute of Chicago (https://api.artic.edu), sin clave.
 *
 * <p>Solo define lo propio de este museo; los pasos comunes los pone {@link FuenteHttpBase}.
 * El buscador de Chicago devuelve muy pocos campos si no se piden, así que se piden con "fields".
 * Su respuesta trae los resultados en el array "data", el formato por defecto de la plantilla.
 */
public class FuenteChicago extends FuenteHttpBase {

    private static final String URL_BUSQUEDA = "https://api.artic.edu/api/v1/artworks/search"
            + "?q=%s&fields=id,title,artist_title,is_on_view&limit=100";

    public FuenteChicago(ClienteHttp cliente) {
        super(cliente);
    }

    @Override
    public String getNombreMuseo() {
        return "Art Institute of Chicago";
    }

    @Override
    protected String construirUrl(String nombreArtista) {
        return String.format(URL_BUSQUEDA, codificar(nombreArtista));
    }

    @Override
    protected Obra convertirAObra(JSONObject json) {
        if (!json.has("id")) {
            return null;
        }
        return new Obra(
                String.valueOf(json.get("id")),
                json.optString("title", ""),
                json.optString("artist_title", ""),
                getNombreMuseo(),
                json.optBoolean("is_on_view", false));
    }
}
