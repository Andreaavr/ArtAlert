package artalert.fuentes;

import artalert.modelo.Obra;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Fuente del Cleveland Museum of Art (https://openaccess-api.clevelandart.org), sin clave.
 *
 * <p>Solo define lo propio de este museo; los pasos comunes los pone {@link FuenteHttpBase}.
 * <ul>
 *   <li>Se busca con el parámetro "artists", que filtra por nombre de artista.</li>
 *   <li>El autor viene en "creators" como texto: "Claude Monet (French, 1840–1926)".</li>
 *   <li>Se considera que la obra está en sala si "current_location" (la sala) tiene valor.</li>
 * </ul>
 * Su respuesta trae los resultados en el array "data", el formato por defecto de la plantilla.
 */
public class FuenteCleveland extends FuenteHttpBase {

    private static final String URL_BUSQUEDA = "https://openaccess-api.clevelandart.org/api/artworks/"
            + "?artists=%s&fields=id,title,creators,current_location&limit=100";

    public FuenteCleveland(ClienteHttp cliente) {
        super(cliente);
    }

    @Override
    public String getNombreMuseo() {
        return "Cleveland Museum of Art";
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
        String sala = json.optString("current_location", "");
        return new Obra(
                String.valueOf(json.get("id")),
                json.optString("title", ""),
                primerAutor(json.optJSONArray("creators")),
                getNombreMuseo(),
                !sala.isBlank());
    }

    /** "Claude Monet (French, 1840–1926)" → "Claude Monet". */
    private static String primerAutor(JSONArray creadores) {
        if (creadores == null || creadores.length() == 0) {
            return "";
        }
        JSONObject primero = creadores.optJSONObject(0);
        if (primero == null) {
            return "";
        }
        String descripcion = primero.optString("description", "");
        int parentesis = descripcion.indexOf('(');
        return parentesis > 0 ? descripcion.substring(0, parentesis).trim() : descripcion.trim();
    }
}