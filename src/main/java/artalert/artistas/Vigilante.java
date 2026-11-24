package artalert.artistas;

import artalert.fuentes.FuenteNoDisponibleException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Pide a cada museo las obras de los artistas que alguien sigue y se las pasa al artista,
 * que es quien avisa a sus observadores.
 *
 * <p>No escribe nada por pantalla (SRP): devuelve los problemas y quien lo use decide cómo
 * mostrarlos. Si un museo falla, se apunta y se sigue con los demás.
 */
public class Vigilante {

    private final CatalogoArtistas catalogo;

    public Vigilante(CatalogoArtistas catalogo) {
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
    }

    /**
     * Comprueba todos los artistas que tienen al menos un seguidor.
     * Los artistas que nadie sigue no se consultan, para no hacer llamadas inútiles.
     *
     * @return mensajes de los museos que no han respondido (vacía si todo ha ido bien)
     */
    public List<String> comprobar() {
        List<String> problemas = new ArrayList<>();
        for (Artista artista : catalogo.getArtistas()) {
            if (artista.tieneSeguidores()) {
                comprobar(artista).ifPresent(problemas::add);
            }
        }
        return List.copyOf(problemas);
    }

    /**
     * Comprueba un solo artista. Sirve también para cargar sus obras justo cuando alguien
     * empieza a seguirlo.
     *
     * @return el problema, si el museo no ha respondido
     */
    public Optional<String> comprobar(Artista artista) {
        try {
            artista.actualizarObras(artista.getFuente().buscarPorArtista(artista.getNombre()));
            return Optional.empty();
        } catch (FuenteNoDisponibleException e) {
            return Optional.of(artista.getMuseo() + " no responde: " + e.getMessage());
        }
    }
}
