package artalert;

import artalert.fuentes.ClienteHttp;
import artalert.fuentes.FuenteCleveland;
import artalert.modelo.Obra;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FuenteCleveland con una respuesta fija con el formato real de la API (sin internet). */
class FuenteClevelandTest {

    private static final String RESPUESTA = """
            {"info": {"total": 3},
             "data": [
               {"id": 135382, "title": "The Red Kerchief",
                "creators": [{"description": "Claude Monet (French, 1840–1926)", "role": "artist"}],
                "current_location": "222 Impressionism"},
               {"id": 135383, "title": "Water Lilies",
                "creators": [{"description": "Claude Monet (French, 1840–1926)", "role": "artist"}],
                "current_location": null},
               {"id": 1, "title": "Otro autor",
                "creators": [{"description": "Thomas Cole (American, 1801-1848)", "role": "artist"}],
                "current_location": null}
             ]}
            """;

    @Test
    void leeAutorYSalaYDescartaOtrosArtistas() {
        String[] urlPedida = new String[1];
        ClienteHttp falso = url -> {
            urlPedida[0] = url;
            return RESPUESTA;
        };

        List<Obra> obras = new FuenteCleveland(falso).buscarPorArtista("Claude Monet");

        assertEquals(2, obras.size());
        assertEquals("Claude Monet", obras.get(0).artista());
        assertTrue(obras.get(0).enSala());
        assertFalse(obras.get(1).enSala());
        assertEquals("Cleveland Museum of Art", obras.get(0).museo());
        assertTrue(urlPedida[0].contains("artists=Claude+Monet"));
    }
}