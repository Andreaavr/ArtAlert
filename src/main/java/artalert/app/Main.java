package artalert.app;

import artalert.artistas.CatalogoArtistas;
import artalert.artistas.ComparadorObras;
import artalert.artistas.Vigilante;
import artalert.fuentes.ClienteHttp;
import artalert.fuentes.ClienteHttpJava;
import artalert.fuentes.FuenteChicago;
import artalert.fuentes.FuenteCleveland;
import artalert.fuentes.FuenteConSimulacion;
import artalert.fuentes.FuenteMet;
import artalert.fuentes.FuenteObras;
import artalert.gui.CargadorImagenes;
import artalert.gui.GeneradorNovedades;
import artalert.gui.VentanaPrincipal;
import artalert.usuarios.ServicioSeguimiento;
import artalert.usuarios.Usuario;

import javax.swing.SwingUtilities;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Punto de entrada de ArtAlert.
 * Aquí solo se crean los objetos y se conectan entre sí; la lógica vive en sus clases (SRP).
 * Para añadir otro museo basta con añadir su fuente a la lista (OCP).
 */
public class Main {

    public static void main(String[] args) {
        // ---- Persona A: museos (Estrategia), cada uno envuelto en el Decorador de simulación ----
        ClienteHttp cliente = new ClienteHttpJava();
        Random azar = new Random();
        Map<String, FuenteConSimulacion> museos = new LinkedHashMap<>();
        for (FuenteObras real : List.of(new FuenteChicago(cliente), new FuenteCleveland(cliente), new FuenteMet(cliente))) {
            museos.put(real.getNombreMuseo(), new FuenteConSimulacion(real, azar));
        }
        List<FuenteObras> fuentes = List.copyOf(museos.values());
        CatalogoArtistas catalogo = new CatalogoArtistas(new ComparadorObras());
        Vigilante vigilante = new Vigilante(catalogo);

        // ---- Persona B: seguimiento, modo demostración e interfaz gráfica ----
        ServicioSeguimiento seguimiento = new ServicioSeguimiento(catalogo, vigilante);
        GeneradorNovedades generador = artista -> museos.get(artista.getMuseo())
                .prepararNovedad(artista.getNombre(), artista.getObras());

        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal.aplicarAspecto();
            Usuario primero = VentanaPrincipal.preguntarPrimerUsuario();
            if (primero != null) {
                new VentanaPrincipal(fuentes, seguimiento, vigilante, new CargadorImagenes(), generador, primero)
                        .setVisible(true);
            }
        });
    }
}