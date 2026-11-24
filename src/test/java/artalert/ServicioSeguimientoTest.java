package artalert;

import artalert.artistas.CatalogoArtistas;
import artalert.artistas.ComparadorObras;
import artalert.artistas.Vigilante;
import artalert.fuentes.FuenteObras;
import artalert.modelo.Obra;
import artalert.usuarios.ServicioSeguimiento;
import artalert.usuarios.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Seguir a un artista en uno o varios museos, con museos falsos (sin internet). */
class ServicioSeguimientoTest {

    private static FuenteObras museo(String nombre, Obra... obras) {
        return new FuenteObras() {
            @Override
            public String getNombreMuseo() {
                return nombre;
            }

            @Override
            public List<Obra> buscarPorArtista(String nombreArtista) {
                return List.of(obras);
            }
        };
    }

    private final CatalogoArtistas catalogo = new CatalogoArtistas(new ComparadorObras());
    private final ServicioSeguimiento servicio = new ServicioSeguimiento(catalogo, new Vigilante(catalogo));

    @Test
    void sigueAlArtistaEnTodosLosMuseosElegidosQueTienenObras() {
        FuenteObras chicago = museo("Chicago", new Obra("1", "Nenúfares", "Monet", "Chicago", true));
        FuenteObras met = museo("Met", new Obra("2", "Puente", "Monet", "Met", false));
        FuenteObras vacio = museo("Vacío");
        Usuario ana = new Usuario("Ana");

        List<String> mensajes = servicio.seguir(ana, "Monet", List.of(chicago, met, vacio));

        assertEquals(2, ana.getSeguidos().size());
        assertEquals(3, mensajes.size());
        assertTrue(mensajes.get(2).startsWith("No hay obras"));
        assertEquals(1, ana.getSeguidos().get(0).getObras().size());
    }

    @Test
    void dosUsuariosQuedanSuscritosAlMismoSujeto() {
        FuenteObras chicago = museo("Chicago", new Obra("1", "Nenúfares", "Monet", "Chicago", true));
        Usuario ana = new Usuario("Ana");
        Usuario luis = new Usuario("Luis");

        servicio.seguir(ana, "Monet", List.of(chicago));
        servicio.seguir(luis, "MONET", List.of(chicago));

        assertTrue(ana.getSeguidos().get(0) == luis.getSeguidos().get(0));
    }

    @Test
    void seguirDosVecesNoDuplica() {
        FuenteObras chicago = museo("Chicago", new Obra("1", "Nenúfares", "Monet", "Chicago", true));
        Usuario ana = new Usuario("Ana");

        servicio.seguir(ana, "Monet", List.of(chicago));
        List<String> mensajes = servicio.seguir(ana, "Monet", List.of(chicago));

        assertEquals(1, ana.getSeguidos().size());
        assertTrue(mensajes.get(0).startsWith("Ya seguías"));
    }
}
