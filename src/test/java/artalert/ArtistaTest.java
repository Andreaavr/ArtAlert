package artalert;

import artalert.artistas.Artista;
import artalert.artistas.ComparadorObras;
import artalert.fuentes.FuenteObras;
import artalert.modelo.EventoObra;
import artalert.modelo.Obra;
import artalert.modelo.TipoEvento;
import artalert.observador.Observador;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comprueba el patrón Observador con una fuente y un observador falsos. */
class ArtistaTest {

    private static final FuenteObras FUENTE_FALSA = new FuenteObras() {
        @Override
        public String getNombreMuseo() {
            return "Museo de prueba";
        }

        @Override
        public List<Obra> buscarPorArtista(String nombreArtista) {
            return List.of();
        }
    };

    /** Observador que solo apunta los avisos que recibe. */
    private static class ObservadorQueApunta implements Observador {
        final List<EventoObra> recibidos = new ArrayList<>();

        @Override
        public void actualizar(EventoObra evento) {
            recibidos.add(evento);
        }
    }

    private static Obra obra(String id, boolean enSala) {
        return new Obra(id, "Obra " + id, "Monet", "Museo de prueba", enSala);
    }

    private final Artista monet = new Artista("Monet", FUENTE_FALSA, new ComparadorObras());

    @Test
    void laPrimeraCargaNoAvisa() {
        ObservadorQueApunta ana = new ObservadorQueApunta();
        monet.suscribir(ana);

        monet.actualizarObras(List.of(obra("1", false)));

        assertTrue(ana.recibidos.isEmpty());
        assertEquals(1, monet.getObras().size());
    }

    @Test
    void avisaDeObraNuevaExpuestaYRetirada() {
        ObservadorQueApunta ana = new ObservadorQueApunta();
        monet.suscribir(ana);
        monet.actualizarObras(List.of(obra("1", false), obra("2", true)));

        monet.actualizarObras(List.of(obra("1", true), obra("2", false), obra("3", false)));

        List<TipoEvento> tipos = ana.recibidos.stream().map(EventoObra::tipo).toList();
        assertEquals(List.of(TipoEvento.EXPUESTA, TipoEvento.RETIRADA, TipoEvento.NUEVA), tipos);
    }

    @Test
    void soloAvisaALosSuscritosYSinDuplicar() {
        ObservadorQueApunta ana = new ObservadorQueApunta();
        ObservadorQueApunta luis = new ObservadorQueApunta();
        monet.suscribir(ana);
        monet.suscribir(ana);
        monet.suscribir(luis);
        monet.desuscribir(luis);
        monet.actualizarObras(List.of());

        monet.actualizarObras(List.of(obra("1", false)));

        assertEquals(1, ana.recibidos.size());
        assertTrue(luis.recibidos.isEmpty());
    }

    @Test
    void sabeSiTieneSeguidores() {
        ObservadorQueApunta ana = new ObservadorQueApunta();
        assertFalse(monet.tieneSeguidores());
        monet.suscribir(ana);
        assertTrue(monet.tieneSeguidores());
        monet.desuscribir(ana);
        assertFalse(monet.tieneSeguidores());
    }
}
