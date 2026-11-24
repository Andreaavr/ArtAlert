package artalert.fuentes;

import artalert.modelo.Obra;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Fuente del Metropolitan Museum of Art de Nueva York (https://collectionapi.metmuseum.org), sin clave.
 *
 * <p>Su API funciona distinto a las otras dos: la búsqueda solo devuelve números de obra
 * ("objectIDs") y los datos de cada obra hay que pedirlos aparte. Por eso sobrescribe el gancho
 * {@link #obtenerResultados(String)} de la plantilla; el resto de pasos no cambia.
 *
 * <p>Para no saturar el servidor (si recibe demasiadas peticiones seguidas responde 403):
 * <ul>
 *   <li>Los datos de cada obra (título, autor, imagen) no cambian, así que se piden una sola vez
 *       y se guardan.</li>
 *   <li>Si está en sala se sabe con una segunda búsqueda con el filtro "isOnView=true".
 *       Así cada comprobación son solo 2 peticiones.</li>
 *   <li>La primera carga pide como mucho {@value #MAXIMO_OBRAS} obras, de
 *       {@value #PETICIONES_A_LA_VEZ} en {@value #PETICIONES_A_LA_VEZ}.</li>
 * </ul>
 */
public class FuenteMet extends FuenteHttpBase {

    static final int MAXIMO_OBRAS = 40;
    private static final int PETICIONES_A_LA_VEZ = 4;

    private static final String URL_BUSQUEDA = "https://collectionapi.metmuseum.org/public/collection/v1.1/search"
            + "?artistOrCulture=true&q=%s&limit=" + MAXIMO_OBRAS;
    private static final String FILTRO_EN_SALA = "&isOnView=true";
    private static final String URL_OBRA = "https://collectionapi.metmuseum.org/public/collection/v1/objects/%d";

    /** Datos de cada obra ya pedida, por su número. */
    private final Map<Long, JSONObject> datosObras = new ConcurrentHashMap<>();
    private final ExecutorService hilos = Executors.newFixedThreadPool(PETICIONES_A_LA_VEZ, tarea -> {
        Thread hilo = new Thread(tarea, "met");
        hilo.setDaemon(true);
        return hilo;
    });

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

    /** Gancho sobrescrito: números de obra, cuáles están en sala y los datos de cada una. */
    @Override
    protected List<JSONObject> obtenerResultados(String url) {
        List<Long> ids = buscarIds(url);
        if (ids.isEmpty()) {
            return List.of();
        }
        Set<Long> enSala = new HashSet<>(buscarIds(url + FILTRO_EN_SALA));
        pedirLasQueFaltan(ids);

        List<JSONObject> resultados = new ArrayList<>();
        for (Long id : ids) {
            JSONObject datos = datosObras.get(id);
            if (datos != null) {
                resultados.add(new JSONObject()
                        .put("objectID", id)
                        .put("title", datos.optString("title", ""))
                        .put("artistDisplayName", datos.optString("artistDisplayName", ""))
                        .put("primaryImageSmall", datos.optString("primaryImageSmall", ""))
                        .put("enSala", enSala.contains(id)));
            }
        }
        return resultados;
    }

    /** Números de obra de una búsqueda (sin resultados la API devuelve "objectIDs": null). */
    private List<Long> buscarIds(String url) {
        JSONArray ids = new JSONObject(pedirJson(url)).optJSONArray("objectIDs");
        List<Long> lista = new ArrayList<>();
        for (int i = 0; ids != null && i < ids.length() && lista.size() < MAXIMO_OBRAS; i++) {
            lista.add(ids.optLong(i));
        }
        return lista;
    }

    /** Pide los datos de las obras que aún no se conocen, unas pocas a la vez. */
    private void pedirLasQueFaltan(List<Long> ids) {
        List<Future<?>> pendientes = new ArrayList<>();
        for (Long id : ids) {
            if (!datosObras.containsKey(id)) {
                pendientes.add(hilos.submit(() -> pedirObra(id).ifPresent(datos -> datosObras.put(id, datos))));
            }
        }
        for (Future<?> pendiente : pendientes) {
            try {
                pendiente.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FuenteNoDisponibleException("Petición interrumpida", e);
            } catch (ExecutionException e) {
                // pedirObra ya captura sus errores; aquí no debería llegar nada
            }
        }
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
        return new Obra(
                String.valueOf(json.get("objectID")),
                json.optString("title", ""),
                json.optString("artistDisplayName", ""),
                getNombreMuseo(),
                json.optBoolean("enSala", false),
                json.optString("primaryImageSmall", ""));
    }
}