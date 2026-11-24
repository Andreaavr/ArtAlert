package artalert.gui;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Descarga las imágenes de las obras en segundo plano, para que la ventana no se congele,
 * y las guarda en memoria para no descargar dos veces la misma.
 */
public class CargadorImagenes {

    private final ExecutorService hilos = Executors.newFixedThreadPool(4, tarea -> {
        Thread hilo = new Thread(tarea, "carga-imagenes");
        hilo.setDaemon(true);
        return hilo;
    });
    private final Map<String, ImageIcon> cache = new ConcurrentHashMap<>();

    /**
     * Pide una imagen ajustada al ancho y alto indicados (sin deformarla).
     * Cuando esté lista, se entrega en el hilo de la interfaz. Si falla, se entrega null.
     */
    public void cargar(String url, int ancho, int alto, Consumer<ImageIcon> alTerminar) {
        String clave = url + "@" + ancho + "x" + alto;
        ImageIcon guardada = cache.get(clave);
        if (guardada != null) {
            alTerminar.accept(guardada);
            return;
        }
        hilos.submit(() -> {
            ImageIcon icono = descargar(url, ancho, alto);
            if (icono != null) {
                cache.put(clave, icono);
            }
            SwingUtilities.invokeLater(() -> alTerminar.accept(icono));
        });
    }

    private static ImageIcon descargar(String url, int ancho, int alto) {
        try {
            HttpURLConnection conexion = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conexion.setRequestProperty("User-Agent", "ArtAlert/1.0 (proyecto educativo)");
            // El servidor de imágenes de Chicago responde 403 si la petición no dice venir de su web.
            conexion.setRequestProperty("Referer", "https://www.artic.edu/");
            conexion.setRequestProperty("AIC-User-Agent", "ArtAlert (proyecto educativo ULL)");
            conexion.setConnectTimeout(10_000);
            conexion.setReadTimeout(15_000);
            try (InputStream datos = conexion.getInputStream()) {
                BufferedImage imagen = ImageIO.read(datos);
                if (imagen == null) {
                    return null;
                }
                double escala = Math.min((double) ancho / imagen.getWidth(), (double) alto / imagen.getHeight());
                int w = Math.max(1, (int) (imagen.getWidth() * escala));
                int h = Math.max(1, (int) (imagen.getHeight() * escala));
                return new ImageIcon(imagen.getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            return null;
        }
    }
}