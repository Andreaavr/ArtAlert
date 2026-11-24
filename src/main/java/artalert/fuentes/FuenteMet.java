package artalert.fuentes;

import artalert.modelo.Obra;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fuente del Metropolitan Museum of Art de Nueva York (https://collectionapi.metmuseum.org), sin clave.
 *
 * <p>Su API funciona distinto a las otras dos: la búsqueda solo devuelve una lista de números
 * ("objectIDs") y luego hay que pedir cada obra por separado. Por eso sobrescribe el gancho
 * {@link #obtenerResultados(String)} de la plantilla; el resto de pasos no cambia.
 * <ul>
 *   <li>Búsqueda v1.1 (la v1 está retirada) limitada al campo de artista con "artistOrCulture".</li>
 *   <li>Se piden como mucho {@value #MAXIMO_OBRAS} obras y en paralelo, para que no tarde.</li>
 *   <li>Se considera que la obra está en sala si tiene número de sala ("GalleryNumber").</li>
 * </ul>
 */
public class FuenteMet extends FuenteHttpBase {

    static final int MAXIMO_OBRAS = 40;

    private static final String URL_BUSQUEDA = "https://collectionapi.metmuseum.org/public/collection/v1.1/search"
            + "?artistOrCulture=true&q=%s&limit=" + MAXIMO_OBRAS;
    private static final String URL_OBRA = "https://collectionapi.metmuseum.org/public/collection/v1/objects/%d";

    public FuenteMet(ClienteHttp cliente) {
        super(cliente);
    }

    @Override
    public String getNombreMuseo() {
        return "The Metropolitan Museum of Art";
    }

    @Override
    protected String construirUrl(String nombreArtista) {
        return String.format(URL_BUSQUEDA, codificar(nombreArtista));
    }

    /** Gancho sobrescrito: primero los números de obra y después cada obra, en paralelo. */
    @Override
    protected List<JSONObject> obtenerResultados(String url) {
        JSONArray ids = new JSONObject(pedirJson(url)).optJSONArray("objectIDs");
        if (ids == null) {
            return List.of(); // sin resultados la API devuelve "objectIDs": null
        }
        List<Long> primeros = new ArrayList<>();
        for (int i = 0; i < ids.length() && primeros.size() < MAXIMO_OBRAS; i++) {
            primeros.add(ids.optLong(i));
        }
        return primeros.parallelStream()
                .map(this::pedirObra)
                .flatMap(Optional::stream)
                .toList();
    }

    /** Una obra concreta. Si esa en particular falla, se salta y se siguen las demás. */
    private Optional<JSONObject> pedirObra(long id) {
        try {
            return Optional.of(new JSONObject(pedirJson(String.format(URL_OBRA, id))));
        } catch (FuenteNoDisponibleException e) {
            return Optional.empty();
        }
    }

    @Override
    protected Obra convertirAObra(JSONObject json) {
        if (!json.has("objectID")) {
            return null;
        }
        String sala = json.optString("GalleryNumber", "");
        return new Obra(
                String.valueOf(json.get("objectID")),
                json.optString("title", ""),
                json.optString("artistDisplayName", ""),
                getNombreMuseo(),
                !sala.isBlank(),
                json.optString("primaryImageSmall", ""));
    }
}
