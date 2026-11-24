package artalert.fuentes;

import artalert.modelo.Obra;
import artalert.util.Textos;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Patrón Plantilla para las fuentes que leen una API HTTP con JSON.
 *
 * <p>{@link #buscarPorArtista(String)} fija los pasos y es {@code final}, así que ninguna
 * subclase puede romper el orden (LSP). Cada museo solo implementa lo que le diferencia:
 * <ol>
 *   <li>{@link #construirUrl(String)}: cómo se busca en su API.</li>
 *   <li>{@link #obtenerResultados(String)}: gancho opcional. Por defecto lee el array "data",
 *       que es lo que devuelven Chicago y Cleveland. The Met lo sobrescribe.</li>
 *   <li>{@link #convertirAObra(JSONObject)}: cómo se traduce su JSON a {@link Obra}.</li>
 *   <li>{@link #esDelArtista(Obra, String)}: gancho opcional que descarta obras de otros autores.</li>
 * </ol>
 */
public abstract class FuenteHttpBase implements FuenteObras {

    private final ClienteHttp cliente;

    protected FuenteHttpBase(ClienteHttp cliente) {
        this.cliente = Objects.requireNonNull(cliente, "cliente");
    }

    /** Método plantilla. */
    @Override
    public final List<Obra> buscarPorArtista(String nombreArtista) {
        if (nombreArtista == null || nombreArtista.isBlank()) {
            return List.of();
        }
        String nombre = nombreArtista.trim();
        String url = construirUrl(nombre);
        List<Obra> obras = new ArrayList<>();
        for (JSONObject json : obtenerResultados(url)) {
            Obra obra = convertirAObra(json);
            if (obra != null && esDelArtista(obra, nombre)) {
                obras.add(obra);
            }
        }
        return List.copyOf(obras);
    }

    /**
     * Gancho: decide si una obra devuelta por la búsqueda es de verdad del artista buscado.
     * Las APIs buscan por texto y pueden colar obras de otros autores (por ejemplo, un cuadro
     * cuyo título menciona a Monet). Por defecto compara sin mayúsculas ni tildes.
     */
    protected boolean esDelArtista(Obra obra, String nombreBuscado) {
        return Textos.normalizar(obra.artista()).contains(Textos.normalizar(nombreBuscado));
    }

    /** URL de búsqueda en la API del museo para este artista. */
    protected abstract String construirUrl(String nombreArtista);

    /** Traduce un resultado de la API a una Obra. Devuelve null si el resultado no sirve. */
    protected abstract Obra convertirAObra(JSONObject json);

    /** Gancho: lista de resultados en JSON. Por defecto, el array "data" de la respuesta. */
    protected List<JSONObject> obtenerResultados(String url) {
        JSONObject respuesta = new JSONObject(pedirJson(url));
        return aLista(respuesta.optJSONArray("data"));
    }

    /** Hace la petición a través del cliente HTTP inyectado. */
    protected final String pedirJson(String url) {
        return cliente.get(url);
    }

    /** Pasa un JSONArray (que puede ser null) a una lista de objetos. */
    protected static List<JSONObject> aLista(JSONArray array) {
        List<JSONObject> lista = new ArrayList<>();
        if (array == null) {
            return lista;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject elemento = array.optJSONObject(i);
            if (elemento != null) {
                lista.add(elemento);
            }
        }
        return lista;
    }

    /** Codifica un texto para meterlo en una URL. */
    protected static String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8);
    }
}
