package artalert.fuentes;

/** El museo no ha respondido o ha respondido con un error. */
public class FuenteNoDisponibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FuenteNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public FuenteNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
