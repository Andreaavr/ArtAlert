package artalert.observador;

import artalert.modelo.EventoObra;

/**
 * Sujeto del patrón Observador: mantiene su lista de observadores y les avisa.
 * Depende de la abstracción Observador, nunca de una clase concreta (DIP).
 */
public interface Sujeto {

    /** Añade un observador. Suscribir dos veces al mismo no debe duplicar avisos. */
    void suscribir(Observador observador);

    /** Quita un observador. Si no estaba suscrito, no hace nada. */
    void desuscribir(Observador observador);

    /** Envía el evento a todos los observadores suscritos en este momento. */
    void notificar(EventoObra evento);
}
