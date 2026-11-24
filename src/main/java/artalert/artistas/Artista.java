package artalert.artistas;

import artalert.fuentes.FuenteObras;
import artalert.modelo.EventoObra;
import artalert.modelo.Obra;
import artalert.observador.Observador;
import artalert.observador.Sujeto;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Sujeto del patrón Observador: un artista dentro de un museo concreto.
 * "Monet en Chicago" y "Monet en The Met" son dos objetos Artista distintos.
 *
 * <p>Responsabilidad (SRP): gestionar a sus observadores y recordar qué obras conoce.
 * Decidir qué ha cambiado lo delega en {@link ComparadorObras}, y pedir las obras al
 * museo lo hace el Vigilante a través de la {@link FuenteObras}.
 */
public class Artista implements Sujeto {

    private final String nombre;
    private final FuenteObras fuente;
    private final ComparadorObras comparador;

    /** LinkedHashSet: no permite duplicados y conserva el orden de suscripción. */
    private final Set<Observador> observadores = new LinkedHashSet<>();
    private final Map<String, Obra> obrasConocidas = new LinkedHashMap<>();
    private boolean primeraCargaHecha = false;

    public Artista(String nombre, FuenteObras fuente, ComparadorObras comparador) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El artista necesita un nombre");
        }
        this.nombre = nombre.trim();
        this.fuente = Objects.requireNonNull(fuente, "fuente");
        this.comparador = Objects.requireNonNull(comparador, "comparador");
    }

    // ---------- Patrón Observador ----------

    @Override
    public synchronized void suscribir(Observador observador) {
        observadores.add(Objects.requireNonNull(observador, "observador"));
    }

    @Override
    public synchronized void desuscribir(Observador observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificar(EventoObra evento) {
        // Se recorre una copia por si algún observador se da de baja mientras recibe el aviso.
        List<Observador> copia;
        synchronized (this) {
            copia = List.copyOf(observadores);
        }
        for (Observador observador : copia) {
            observador.actualizar(evento);
        }
    }

    public synchronized boolean tieneSeguidores() {
        return !observadores.isEmpty();
    }

    // ---------- Obras ----------

    /**
     * Recibe las obras que devuelve ahora el museo, avisa de lo que haya cambiado y las recuerda.
     * La primera vez solo guarda las obras: lo que ya existía no es una novedad.
     */
    public void actualizarObras(List<Obra> obras) {
        List<EventoObra> eventos;
        synchronized (this) {
            eventos = comparador.comparar(obrasConocidas, obras);
            for (Obra obra : obras) {
                obrasConocidas.put(obra.clave(), obra);
            }
            if (!primeraCargaHecha) {
                primeraCargaHecha = true;
                return;
            }
        }
        // Se avisa fuera del bloqueo, para no retener el artista mientras los observadores trabajan.
        for (EventoObra evento : eventos) {
            notificar(evento);
        }
    }

    /** Copia de solo lectura de las obras conocidas (para mostrarlas al usuario). */
    public synchronized List<Obra> getObras() {
        return List.copyOf(obrasConocidas.values());
    }

    public String getNombre() {
        return nombre;
    }

    public FuenteObras getFuente() {
        return fuente;
    }

    public String getMuseo() {
        return fuente.getNombreMuseo();
    }

    @Override
    public String toString() {
        return nombre + " (" + getMuseo() + ")";
    }
}