package artalert.app;

import artalert.artistas.CatalogoArtistas;
import artalert.artistas.ComparadorObras;
import artalert.artistas.Vigilante;
import artalert.fuentes.ClienteHttp;
import artalert.fuentes.ClienteHttpJava;
import artalert.fuentes.FuenteChicago;
import artalert.fuentes.FuenteCleveland;
import artalert.fuentes.FuenteConSimulacion;
import artalert.fuentes.FuenteObras;
import artalert.gui.CargadorImagenes;
import artalert.gui.GeneradorNovedades;
import artalert.gui.VentanaPrincipal;
import artalert.usuarios.Usuario;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Punto de entrada de ArtAlert.
 * Aquí solo se crean los objetos y se conectan entre sí; la lógica vive en sus clases (SRP).
 */
public class Main {

    public static void main(String[] args) {
        // ---- Persona A: fuentes (decoradas para poder simular novedades), catálogo y vigilante ----
        ClienteHttp cliente = new ClienteHttpJava();
        FuenteConSimulacion chicago = new FuenteConSimulacion(new FuenteChicago(cliente), new Random());
        FuenteConSimulacion cleveland = new FuenteConSimulacion(new FuenteCleveland(cliente), new Random());
        List<FuenteObras> fuentes = List.of(chicago, cleveland);
        CatalogoArtistas catalogo = new CatalogoArtistas(new ComparadorObras());
        Vigilante vigilante = new Vigilante(catalogo);

        Map<String, FuenteConSimulacion> simuladores = Map.of(
                chicago.getNombreMuseo(), chicago,
                cleveland.getNombreMuseo(), cleveland);
        GeneradorNovedades generador = artista -> simuladores.get(artista.getMuseo())
                .prepararNovedad(artista.getNombre(), artista.getObras());

        // ---- Persona B: interfaz gráfica ----
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal.aplicarAspecto();
            Usuario primero = VentanaPrincipal.preguntarPrimerUsuario();
            if (primero != null) {
                new VentanaPrincipal(fuentes, catalogo, vigilante, new CargadorImagenes(), generador, primero)
                        .setVisible(true);
            }
        });
    }
}