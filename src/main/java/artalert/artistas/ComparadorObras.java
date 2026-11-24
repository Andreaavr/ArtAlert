package artalert.artistas;

import artalert.modelo.EventoObra;
import artalert.modelo.Obra;
import artalert.modelo.TipoEvento;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compara las obras que ya se conocían con las que acaba de devolver el museo
 * y decide qué avisos hay que dar.
 *
 * <p>Su única responsabilidad es comparar (SRP): no guarda estado ni avisa a nadie.
 */
public class ComparadorObras {

    /**
     * @param conocidas obras que ya se conocían, por su {@link Obra#clave()}
     * @param nuevas    obras que devuelve ahora el museo
     * @return avisos a enviar (puede estar vacía, nunca es null)
     */
    public List<EventoObra> comparar(Map<String, Obra> conocidas, List<Obra> nuevas) {
        List<EventoObra> eventos = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        for (Obra nueva : nuevas) {
            if (!vistas.add(nueva.clave())) {
                continue; // la API la ha repetido: un solo aviso por obra
            }
            Obra anterior = conocidas.get(nueva.clave());
            if (anterior == null) {
                eventos.add(new EventoObra(TipoEvento.NUEVA, nueva));
            } else if (!anterior.enSala() && nueva.enSala()) {
                eventos.add(new EventoObra(TipoEvento.EXPUESTA, nueva));
            } else if (anterior.enSala() && !nueva.enSala()) {
                eventos.add(new EventoObra(TipoEvento.RETIRADA, nueva));
            }
        }
        return List.copyOf(eventos);
    }
}