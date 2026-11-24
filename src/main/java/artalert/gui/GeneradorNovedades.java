package artalert.gui;

import artalert.artistas.Artista;

/**
 * Lo que la ventana necesita para el botón de demostración: provocar una novedad en un artista.
 * La ventana depende de esta interfaz y no de cómo se simula (DIP). Main la conecta con
 * el decorador {@code FuenteConSimulacion}.
 */
@FunctionalInterface
public interface GeneradorNovedades {

    void prepararNovedad(Artista artista);
}
