package artalert;

import artalert.fuentes.ClienteHttp;
import artalert.fuentes.FuenteMet;
import artalert.fuentes.FuenteNoDisponibleException;
import artalert.modelo.Obra;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FuenteMet con respuestas fijas con el formato real de la API (sin internet). */
class FuenteMetTest {

    /** Cliente falso: la búsqueda da 4 números y cada número devuelve su obra. */
    private static final ClienteHttp FALSO = url -> {
        if (url.contains("/search")) {
            return "{\"total\": 4, \"objectIDs\": [437133, 438003, 999, 404]}";
        }
        if (url.endsWith("/437133")) {
            return "{\"objectID\": 437133, \"title\": \"Bridge over a Pond of Water Lilies\","
                    + " \"artistDisplayName\": \"Claude Monet\", \"GalleryNumber\": \"819\","
                    + " \"primaryImageSmall\": \"https://images.metmuseum.org/a.jpg\"}";
        }
        if (url.endsWith("/438003")) {
            return "{\"objectID\": 438003, \"title\": \"Garden at Sainte-Adresse\","
                    + " \"artistDisplayName\": \"Claude Monet\", \"GalleryNumber\": \"\","
                    + " \"primaryImageSmall\": \"\"}";
        }
        if (url.endsWith("/999")) {
            return "{\"objectID\": 999, \"title\": \"Portrait of Monet\","
                    + " \"artistDisplayName\": \"Pierre-Auguste Renoir\", \"GalleryNumber\": \"820\"}";
        }
        throw new FuenteNoDisponibleException("Respuesta 404 al pedir " + url);
    };

    @Test
    void pideCadaObraYDescartaLasDeOtrosArtistasYLasQueFallan() {
        List<Obra> obras = new FuenteMet(FALSO).buscarPorArtista("Monet").stream()
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
