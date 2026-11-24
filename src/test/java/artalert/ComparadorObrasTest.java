package artalert;

import artalert.artistas.ComparadorObras;
import artalert.modelo.EventoObra;
import artalert.modelo.Obra;
import artalert.modelo.TipoEvento;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Qué avisos se generan al comparar lo que se conocía con lo que llega del museo. */
class ComparadorObrasTest {

    private final ComparadorObras comparador = new ComparadorObras();

    @Test
    void sinCambiosNoHayAvisos() {
        Obra obra = new Obra("1", "Nenúfares", "Monet", "Chicago", true);

        assertTrue(comparador.comparar(Map.of(obra.clave(), obra), List.of(obra)).isEmpty());
    }

    @Test
    void siLaApiRepiteUnaObraSoloSeAvisaUnaVez() {
        Obra obra = new Obra("1", "Nenúfares", "Monet", "Chicago", true);

        List<EventoObra> eventos = comparador.comparar(Map.of(), List.of(obra, obra));

        assertEquals(1, eventos.size());
        assertEquals(TipoEvento.NUEVA, eventos.get(0).tipo());
    }
}
