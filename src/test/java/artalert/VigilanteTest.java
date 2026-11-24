package artalert;

import artalert.artistas.Artista;
import artalert.artistas.CatalogoArtistas;
import artalert.artistas.ComparadorObras;
import artalert.artistas.Vigilante;
import artalert.fuentes.FuenteNoDisponibleException;
import artalert.fuentes.FuenteObras;
import artalert.modelo.EventoObra;
import artalert.modelo.Obra;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Catálogo y Vigilante con museos falsos: sin internet. */
class VigilanteTest {

    /** Museo falso al que se le puede cambiar lo que devuelve. */
    private static class MuseoFalso implements FuenteObras {
        private final String nombre;
        List<Obra> obras = List.of();
        int llamadas = 0;

        MuseoFalso(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public String getNombreMuseo() {
            return nombre;
        }

        @Override
        public List<Obra> buscarPorArtista(String nombreArtista) {
            llamadas++;
            return obras;
        }
    }

    private static class MuseoCaido implements FuenteObras {
        @Override
        public String getNombreMuseo() {
            return "Museo caído";
        }

        @Override
        public List<Obra> buscarPorArtista(String nombreArtista) {
            throw new FuenteNoDisponibleException("sin conexión");
        }
    }

    private final CatalogoArtistas catalogo = new CatalogoArtistas(new ComparadorObras());
    private final Vigilante vigilante = new Vigilante(catalogo);

    @Test
    void elMismoArtistaYMuseoEsSiempreElMismoObjeto() {
        MuseoFalso chicago = new MuseoFalso("Chicago");
        MuseoFalso met = new MuseoFalso("The Met");

        assertTrue(catalogo.obtener("Monet", chicago) == catalogo.obtener("MONET ", chicago));
        assertTrue(catalogo.obtener("Monet", chicago) != catalogo.obtener("Monet", met));
        assertEquals(2, catalogo.getArtistas().size());
    }

    @Test
    void soloConsultaLosArtistasQueTienenSeguidores() {
        MuseoFalso chicago = new MuseoFalso("Chicago");
        catalogo.obtener("Monet", chicago);

        vigilante.comprobar();

        assertEquals(0, chicago.llamadas);
    }

    @Test
    void avisaAlSeguidorCuandoAparecenObrasNuevas() {
        MuseoFalso chicago = new MuseoFalso("Chicago");
        Artista monet = catalogo.obtener("Monet", chicago);
        List<EventoObra> avisos = new ArrayList<>();
        monet.suscribir(avisos::add);

        vigilante.comprobar();
        chicago.obras = List.of(new Obra("1", "Nenúfares", "Monet", "Chicago", true));
        vigilante.comprobar();

        assertEquals(1, avisos.size());
    }

    @Test
    void siUnMuseoFallaSigueConLosDemas() {
        MuseoFalso chicago = new MuseoFalso("Chicago");
        catalogo.obtener("Monet", new MuseoCaido()).suscribir(e -> { });
        catalogo.obtener("Monet", chicago).suscribir(e -> { });

        List<String> problemas = vigilante.comprobar();

        assertEquals(1, problemas.size());
        assertEquals(1, chicago.llamadas);
    }
}
