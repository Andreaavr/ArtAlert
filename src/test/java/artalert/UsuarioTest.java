package artalert;

import artalert.artistas.Artista;
import artalert.artistas.ComparadorObras;
import artalert.fuentes.FuenteObras;
import artalert.modelo.Obra;
import artalert.usuarios.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Usuario como observador: seguir, dejar de seguir y recibir avisos. */
class UsuarioTest {

    private static FuenteObras museo(String nombre) {
        return new FuenteObras() {
            @Override
            public String getNombreMuseo() {
                return nombre;
            }

            @Override
            public List<Obra> buscarPorArtista(String nombreArtista) {
                return List.of();
            }
        };
    }

    private final Artista monetChicago = new Artista("Monet", museo("Chicago"), new ComparadorObras());
    private final Artista monetCleveland = new Artista("Monet", museo("Cleveland"), new ComparadorObras());

    private static Obra obra(String id) {
        return new Obra(id, "Obra " + id, "Monet", "Chicago", false);
    }

    @Test
    void puedeNoSeguirANadie() {
        Usuario ana = new Usuario("Ana");
        assertTrue(ana.getSeguidos().isEmpty());
        assertEquals(0, ana.contarAvisosNuevos());
    }

    @Test
    void puedeSeguirAVariosArtistasDeDistintosMuseos() {
        Usuario ana = new Usuario("Ana");

        assertTrue(ana.seguir(monetChicago));
        assertTrue(ana.seguir(monetCleveland));
        assertFalse(ana.seguir(monetChicago));

        assertEquals(2, ana.getSeguidos().size());
        assertTrue(monetChicago.tieneSeguidores());
    }

    @Test
    void recibeAvisosSoloMientrasSigueAlArtista() {
        Usuario ana = new Usuario("Ana");
        ana.seguir(monetChicago);
        monetChicago.actualizarObras(List.of());

        monetChicago.actualizarObras(List.of(obra("1")));
        assertEquals(1, ana.contarAvisosNuevos());

        ana.dejarDeSeguir(monetChicago);
        monetChicago.actualizarObras(List.of(obra("1"), obra("2")));
        assertEquals(1, ana.contarAvisosNuevos());
        assertFalse(monetChicago.tieneSeguidores());
    }

    @Test
    void losAvisosLeidosNoVuelvenASalir() {
        Usuario ana = new Usuario("Ana");
        ana.seguir(monetChicago);
        monetChicago.actualizarObras(List.of());
        monetChicago.actualizarObras(List.of(obra("1")));

        assertEquals(1, ana.recogerAvisosNuevos().size());
        assertTrue(ana.recogerAvisosNuevos().isEmpty());
        assertEquals(1, ana.getHistorialAvisos().size());
    }
}
