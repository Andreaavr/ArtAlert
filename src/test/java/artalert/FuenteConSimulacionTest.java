package artalert;

import artalert.artistas.Artista;
import artalert.artistas.ComparadorObras;
import artalert.fuentes.FuenteConSimulacion;
import artalert.fuentes.FuenteObras;
import artalert.modelo.EventoObra;
import artalert.modelo.Obra;
import artalert.modelo.TipoEvento;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El decorador no cambia nada hasta que se prepara una novedad, y entonces se avisa. */
class FuenteConSimulacionTest {

    private static final FuenteObras MUSEO = new FuenteObras() {
        @Override
        public String getNombreMuseo() {
            return "Chicago";
        }

        @Override
        public List<Obra> buscarPorArtista(String nombreArtista) {
            return List.of(new Obra("1", "Nenúfares", "Monet", "Chicago", false));
        }
    };

    @Test
    void sinPrepararNadaSeComportaComoLaFuenteOriginal() {
        FuenteConSimulacion simulada = new FuenteConSimulacion(MUSEO, new Random(1));

        assertEquals(MUSEO.buscarPorArtista("Monet"), simulada.buscarPorArtista("Monet"));
        assertEquals("Chicago", simulada.getNombreMuseo());
    }

    @Test
    void unaNovedadPreparadaLlegaComoAvisoAlObservador() {
        FuenteConSimulacion simulada = new FuenteConSimulacion(MUSEO, new Random(1));
        Artista monet = new Artista("Monet", simulada, new ComparadorObras());
        List<EventoObra> avisos = new ArrayList<>();
        monet.suscribir(avisos::add);
        monet.actualizarObras(simulada.buscarPorArtista("Monet"));

        simulada.prepararNovedad("Monet", monet.getObras());
        monet.actualizarObras(simulada.buscarPorArtista("Monet"));

        List<TipoEvento> tipos = avisos.stream().map(EventoObra::tipo).toList();
        assertTrue(tipos.contains(TipoEvento.NUEVA));
        assertTrue(tipos.contains(TipoEvento.EXPUESTA));
    }
}