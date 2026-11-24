package artalert.fuentes;

import artalert.modelo.Obra;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Patrón Decorador: envuelve cualquier {@link FuenteObras} y le añade la posibilidad de
 * simular novedades, sin modificar la fuente original (OCP).
 *
 * <p>Sirve para la demostración: en un museo real una obra puede tardar semanas en cambiar
 * de sala, así que con {@link #prepararNovedad(String, List)} se provoca un cambio y se ve
 * cómo el patrón Observador avisa a los usuarios. Mientras no se prepare nada, se comporta
 * exactamente igual que la fuente que envuelve (LSP).
 */
public class FuenteConSimulacion implements FuenteObras {

    private final FuenteObras fuente;
    private final Random azar;

    /** Obras inventadas que se añaden a los resultados, por artista. */
    private final Map<String, List<Obra>> obrasInventadas = new HashMap<>();
    /** Claves de las obras reales a las que se les invierte el estado de sala. */
    private final Set<String> salaInvertida = new HashSet<>();
    private int contador = 0;

    public FuenteConSimulacion(FuenteObras fuente, Random azar) {
        this.fuente = Objects.requireNonNull(fuente, "fuente");
        this.azar = Objects.requireNonNull(azar, "azar");
    }

    @Override
    public String getNombreMuseo() {
        return fuente.getNombreMuseo();
    }

    @Override
    public List<Obra> buscarPorArtista(String nombreArtista) {
        List<Obra> resultado = new ArrayList<>();
        for (Obra obra : fuente.buscarPorArtista(nombreArtista)) {
            resultado.add(salaInvertida.contains(obra.clave()) ? obra.conEnSala(!obra.enSala()) : obra);
        }
        resultado.addAll(obrasInventadas.getOrDefault(clave(nombreArtista), List.of()));
        return List.copyOf(resultado);
    }

    /**
     * Prepara cambios que aparecerán en la próxima búsqueda de ese artista:
     * una obra nueva en la colección y, si hay obras, una que cambia de sala.
     *
     * @param obrasActuales obras que se conocen ahora del artista (para elegir cuál cambia)
     */
    public void prepararNovedad(String nombreArtista, List<Obra> obrasActuales) {
        contador++;
        Obra inventada = new Obra("simulada-" + contador, "Obra recién adquirida nº " + contador,
                nombreArtista, getNombreMuseo(), false);
        obrasInventadas.computeIfAbsent(clave(nombreArtista), k -> new ArrayList<>()).add(inventada);

        List<Obra> reales = obrasActuales.stream().filter(o -> !o.id().startsWith("simulada-")).toList();
        if (!reales.isEmpty()) {
            String elegida = reales.get(azar.nextInt(reales.size())).clave();
            if (!salaInvertida.remove(elegida)) {
                salaInvertida.add(elegida);
            }
        }
    }

    private static String clave(String nombre) {
        String sinTildes = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim();
    }
}