package artalert.fuentes;

import artalert.modelo.Obra;

import java.util.List;

/**
 * Estrategia para obtener obras de un museo. Cada museo es una implementación intercambiable,
 * así que añadir un museo nuevo no obliga a cambiar el resto del programa (OCP).
 *
 * <p>Contrato que toda implementación debe respetar (LSP):
 * <ul>
 *   <li>Nunca devuelve null: si no hay resultados, devuelve una lista vacía.</li>
 *   <li>Si el museo no responde, lanza {@link FuenteNoDisponibleException}.</li>
 * </ul>
 */
public interface FuenteObras {

    /** Nombre del museo, por ejemplo "Art Institute of Chicago". */
    String getNombreMuseo();

    /** Obras del artista indicado en este museo. */
    List<Obra> buscarPorArtista(String nombreArtista);
}
