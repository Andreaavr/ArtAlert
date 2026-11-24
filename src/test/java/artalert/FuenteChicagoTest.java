package artalert;

import artalert.fuentes.ClienteHttp;
import artalert.fuentes.FuenteChicago;
import artalert.modelo.Obra;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FuenteChicago con una respuesta fija con el formato real de la API (sin internet). */
class FuenteChicagoTest {

    private static final String RESPUESTA = """
            {"pagination": {"total": 3},
             "data": [
               {"id": 16568, "title": "Water Lilies", "artist_title": "Claude Monet", "is_on_view": true},
               {"id": 14598, "title": "The Beach at Sainte-Adresse", "artist_title": "Claude Monet", "is_on_view": false},
               {"id": 99999, "title": "Retrato de Monet", "artist_title": "Pierre-Auguste Renoir", "is_on_view": true},
               {"id": 11111, "title": "Sin autor", "artist_title": null, "is_on_view": false}
             ]}
            """;

    @Test
    void leeLasObrasYDescartaLasDeOtrosArtistas() {
        String[] urlPedida = new String[1];
        ClienteHttp falso = url -> {
            urlPedida[0] = url;
            return RESPUESTA;
        };

        List<Obra> obras = new FuenteChicago(falso).buscarPorArtista("Monet");

        assertEquals(2, obras.size());
        assertEquals("Water Lilies", obras.get(0).titulo());
        assertTrue(obras.get(0).enSala());
        assertEquals("Art Institute of Chicago", obras.get(0).museo());
        assertTrue(urlPedida[0].contains("q=Monet"));
        assertTrue(urlPedida[0].contains("fields=id,title,artist_title,is_on_view"));
    }
}