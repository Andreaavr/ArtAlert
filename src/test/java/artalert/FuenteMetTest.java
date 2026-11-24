package artalert;

import artalert.fuentes.ClienteHttp;
import artalert.fuentes.FuenteMet;
import artalert.fuentes.FuenteNoDisponibleException;
import artalert.modelo.Obra;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FuenteMet con respuestas fijas con el formato real de la API (sin internet). */
class FuenteMetTest {

    /** Cliente falso que cuenta cuántas obras se piden. */
    private static class MetFalso implements ClienteHttp {
        final AtomicInteger obrasPedidas = new AtomicInteger();

        @Override
        public String get(String url) {
            if (url.contains("/search") && url.contains("isOnView=true")) {
                return "{\"total\": 2, \"objectIDs\": [437133, 999]}";
            }
            if (url.contains("/search")) {
                return "{\"total\": 4, \"objectIDs\": [437133, 438003, 999, 404]}";
            }
            obrasPedidas.incrementAndGet();
            if (url.endsWith("/437133")) {
                return "{\"objectID\": 437133, \"title\": \"Bridge over a Pond of Water Lilies\","
                        + " \"artistDisplayName\": \"Claude Monet\","
                        + " \"primaryImageSmall\": \"https://images.metmuseum.org/a.jpg\"}";
            }
            if (url.endsWith("/438003")) {
                return "{\"objectID\": 438003, \"title\": \"Garden at Sainte-Adresse\","
                        + " \"artistDisplayName\": \"Claude Monet\", \"primaryImageSmall\": \"\"}";
            }
            if (url.endsWith("/999")) {
                return "{\"objectID\": 999, \"title\": \"Portrait of Monet\","
                        + " \"artistDisplayName\": \"Pierre-Auguste Renoir\"}";
            }
            throw new FuenteNoDisponibleException("Respuesta 404 al pedir " + url);
        }
    }

    @Test
    void leeLasObrasSabeCualesEstanEnSalaYDescartaLasDeOtrosYLasQueFallan() {
        List<Obra> obras = new FuenteMet(new MetFalso()).buscarPorArtista("Monet").stream()
                .sorted(Comparator.comparing(Obra::id))
                .toList();

        assertEquals(2, obras.size());
        Obra puente = obras.get(0);
        assertEquals("Bridge over a Pond of Water Lilies", puente.titulo());
        assertTrue(puente.enSala());
        assertTrue(puente.tieneImagen());
        assertEquals("The Metropolitan Museum of Art", puente.museo());
        Obra jardin = obras.get(1);
        assertFalse(jardin.enSala());
        assertFalse(jardin.tieneImagen());
    }

    @Test
    void laSegundaComprobacionNoVuelveAPedirLasObrasQueYaConoce() {
        MetFalso falso = new MetFalso();
        FuenteMet met = new FuenteMet(falso);

        met.buscarPorArtista("Monet");
        int despuesDeLaPrimera = falso.obrasPedidas.get();
        met.buscarPorArtista("Monet");

        assertEquals(4, despuesDeLaPrimera);
        assertEquals(5, falso.obrasPedidas.get()); // solo reintenta la que falló (404)
    }

    @Test
    void sinResultadosDevuelveListaVacia() {
        ClienteHttp vacio = url -> "{\"total\": 0, \"objectIDs\": null}";

        assertTrue(new FuenteMet(vacio).buscarPorArtista("Nadie").isEmpty());
    }

    @Test
    void buscaSoloEnElCampoDeArtistaConLaVersionNueva() {
        String[] busqueda = new String[1];
        ClienteHttp espia = url -> {
            busqueda[0] = url;
            return "{\"total\": 0, \"objectIDs\": []}";
        };

        new FuenteMet(espia).buscarPorArtista("Van Gogh");

        assertTrue(busqueda[0].contains("/v1.1/search"));
        assertTrue(busqueda[0].contains("artistOrCulture=true"));
        assertTrue(busqueda[0].contains("q=Van+Gogh"));
    }
}