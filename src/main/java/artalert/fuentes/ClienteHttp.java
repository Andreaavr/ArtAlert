package artalert.fuentes;

/**
 * Hace peticiones HTTP. Las fuentes dependen de esta interfaz y no de una librería concreta (DIP),
 * lo que además permite probarlas con un cliente falso sin conectarse a internet.
 */
public interface ClienteHttp {

    /**
     * Hace un GET y devuelve el cuerpo de la respuesta.
     *
     * @throws FuenteNoDisponibleException si no hay conexión o la respuesta no es 200
     */
    String get(String url);
}
