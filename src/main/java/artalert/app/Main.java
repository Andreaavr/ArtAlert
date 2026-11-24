package artalert.app;

import artalert.artistas.Artista;
import artalert.artistas.CatalogoArtistas;
import artalert.artistas.ComparadorObras;
import artalert.artistas.Vigilante;
import artalert.fuentes.ClienteHttp;
import artalert.fuentes.ClienteHttpJava;
import artalert.fuentes.FuenteChicago;
import artalert.fuentes.FuenteObras;

import java.util.List;

/**
 * Punto de entrada de ArtAlert.
 * Aquí solo se crean los objetos y se conectan entre sí; la lógica vive en sus clases (SRP).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("ArtAlert: avisos de arte con el patrón Observador");

        // ---- Persona A: fuentes, catálogo y vigilante ----
        ClienteHttp cliente = new ClienteHttpJava();
        List<FuenteObras> fuentes = List.of(new FuenteChicago(cliente));
        CatalogoArtistas catalogo = new CatalogoArtistas(new ComparadorObras());
        Vigilante vigilante = new Vigilante(catalogo);

        // Prueba rápida mientras no hay menú: seguir a Monet en Chicago y cargar sus obras.
        Artista monet = catalogo.obtener("Claude Monet", fuentes.get(0));
        monet.suscribir(evento -> System.out.println("  Aviso: " + evento.descripcion()));
        vigilante.comprobar(monet).ifPresent(System.out::println);

        System.out.println(monet + ": " + monet.getObras().size() + " obras");
        monet.getObras().stream().limit(5).forEach(obra ->
                System.out.println("  - " + obra.titulo() + (obra.enSala() ? " (en sala)" : "")));

        // ---- Persona B: usuarios y menú ----
    }
}
