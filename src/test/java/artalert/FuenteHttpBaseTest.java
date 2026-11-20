package artalert;

import artalert.fuentes.ClienteHttp;
import artalert.fuentes.FuenteHttpBase;
import artalert.modelo.Obra;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba el método plantilla sin conectarse a internet:
 * gracias a ClienteHttp (DIP) se usa un cliente falso con una respuesta fija.
 */
class FuenteHttpBaseTest {

    /** Museo de prueba que lee el formato {"data": [...]}. */
    private static class FuentePrueba extends FuenteHttpBase {
        FuentePrueba(ClienteHttp cliente) {
            super(cliente);
        }

        @Override
        public String getNombreMuseo() {
            return "Museo de prueba";
        }

        @Override
        protected String construirUrl(String nombreArtista) {
            return "https://ejemplo.test/buscar?q=" + codificar(nombreArtista);
        }

        @Override
        protected Obra convertirAObra(JSONObject json) {
            if (!json.has("id")) {
                return null;
            }
            return new Obra(String.valueOf(json.get("id")), json.optString("title"),
                    "Monet", getNombreMuseo(), json.optBoolean("is_on_view"));
        }
    }

    @Test
    void convierteCadaResultadoValidoEnUnaObra() {
        ClienteHttp falso = url -> "{\"data\":[{\"id\":1,\"title\":\"Nenúfares\",\"is_on_view\":true},"
                + "{\"title\":\"sin id, se descarta\"},{\"id\":2}]}";

        List<Obra> obras = new FuentePrueba(falso).buscarPorArtista("Monet");

        assertEquals(2, obras.size());
        assertEquals("Nenúfares", obras.get(0).titulo());
        assertTrue(obras.get(0).enSala());
        assertEquals("Sin título", obras.get(1).titulo());
    }

    @Test
    void sinNombreDevuelveListaVaciaSinLlamarALaApi() {
        ClienteHttp quePetaria = url -> {
            throw new AssertionError("No debería llamarse a la API");
        };

        assertTrue(new FuentePrueba(quePetaria).buscarPorArtista("  ").isEmpty());
    }

    @Test
    void respuestaSinDatosDevuelveListaVacia() {
        ClienteHttp falso = url -> "{\"data\":[]}";

        assertTrue(new FuentePrueba(falso).buscarPorArtista("Monet").isEmpty());
    }
}
