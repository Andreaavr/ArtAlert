package artalert.usuarios;

import artalert.artistas.Artista;
import artalert.artistas.CatalogoArtistas;
import artalert.artistas.Vigilante;
import artalert.fuentes.FuenteObras;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso «seguir a un artista en uno o varios museos».
 *
 * <p>Saca esta regla de la interfaz (SRP): la ventana solo pregunta y muestra, y aquí se decide
 * qué pasa. Por cada museo elegido se obtiene el artista del catálogo (siempre el mismo objeto),
 * se cargan sus obras la primera vez y, si tiene alguna, el usuario se suscribe como observador.
 */
public class ServicioSeguimiento {

    private final CatalogoArtistas catalogo;
    private final Vigilante vigilante;

    public ServicioSeguimiento(CatalogoArtistas catalogo, Vigilante vigilante) {
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
        this.vigilante = Objects.requireNonNull(vigilante, "vigilante");
    }

    /**
     * Hace que el usuario siga al artista en los museos indicados (pueden ser uno, varios o todos).
     * Hace llamadas a los museos, así que conviene llamarlo fuera del hilo de la ventana.
     *
     * @return un mensaje por museo con lo que ha pasado, para mostrarlo al usuario
     */
    public List<String> seguir(Usuario usuario, String nombreArtista, List<FuenteObras> museos) {
        Objects.requireNonNull(usuario, "usuario");
        List<String> mensajes = new ArrayList<>();
        for (FuenteObras museo : museos) {
            Artista artista = catalogo.obtener(nombreArtista, museo);
            if (artista.getObras().isEmpty()) {
                // Primera carga: se guardan las obras sin avisar (lo que ya existía no es novedad).
                vigilante.comprobar(artista).ifPresent(mensajes::add);
            }
            if (artista.getObras().isEmpty()) {
                mensajes.add("No hay obras de " + nombreArtista + " en " + museo.getNombreMuseo());
            } else if (usuario.seguir(artista)) {
                mensajes.add("Ahora sigues a " + artista);
            } else {
                mensajes.add("Ya seguías a " + artista);
            }
        }
        return List.copyOf(mensajes);
    }
}
