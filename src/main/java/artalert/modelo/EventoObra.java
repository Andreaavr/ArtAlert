package artalert.modelo;

import java.util.Objects;

/**
 * Aviso que el sujeto envía a sus observadores.
 *
 * @param tipo qué ha pasado
 * @param obra obra afectada
 */
public record EventoObra(TipoEvento tipo, Obra obra) {

    public EventoObra {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(obra, "obra");
    }

    /** Texto listo para mostrar al usuario. */
    public String descripcion() {
        String accion = switch (tipo) {
            case NUEVA -> "Nueva obra en la colección";
            case EXPUESTA -> "Ahora expuesta en sala";
            case RETIRADA -> "Retirada de sala";
        };
        return "[" + obra.museo() + "] " + accion + ": \"" + obra.titulo() + "\" de " + obra.artista();
    }
}
