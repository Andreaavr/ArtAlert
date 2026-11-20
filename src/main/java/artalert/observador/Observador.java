package artalert.observador;

import artalert.modelo.EventoObra;

/**
 * Observador del patrón Observador: recibe los avisos de los sujetos a los que está suscrito.
 * Una sola responsabilidad y un solo método (ISP): quien observa no necesita saber nada más.
 */
public interface Observador {

    /** Lo llama el sujeto cada vez que ocurre algo que el observador debe saber. */
    void actualizar(EventoObra evento);
}
