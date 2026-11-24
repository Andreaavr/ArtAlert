package artalert.usuarios;

import artalert.artistas.Artista;
import artalert.modelo.EventoObra;
import artalert.observador.Observador;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Observador del patrón Observador: una persona que sigue a los artistas que elige.
 * Puede seguir a ninguno, a uno o a todos los que quiera, de cualquier museo.
 *
 * <p>Responsabilidad (SRP): recordar a quién sigue y guardar los avisos que recibe.
 * No muestra nada; eso lo hace la interfaz. Sus métodos están sincronizados porque los avisos
 * llegan desde el hilo que consulta los museos mientras la ventana los lee.
 */
public class Usuario implements Observador {

    private final String nombre;
    private final Set<Artista> seguidos = new LinkedHashSet<>();
    private final List<EventoObra> avisos = new ArrayList<>();
    private int avisosLeidos = 0;

    public Usuario(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El usuario necesita un nombre");
        }
        this.nombre = nombre.trim();
    }

    // ---------- Seguir artistas ----------

    /**
     * Empieza a seguir a un artista: se suscribe como observador.
     *
     * @return false si ya lo seguía
     */
    public synchronized boolean seguir(Artista artista) {
        Objects.requireNonNull(artista, "artista");
        if (!seguidos.add(artista)) {
            return false;
        }
        artista.suscribir(this);
        return true;
    }

    /**
     * Deja de seguir a un artista: se da de baja como observador.
     *
     * @return false si no lo seguía
     */
    public synchronized boolean dejarDeSeguir(Artista artista) {
        if (!seguidos.remove(artista)) {
            return false;
        }
        artista.desuscribir(this);
        return true;
    }

    public synchronized boolean sigueA(Artista artista) {
        return seguidos.contains(artista);
    }

    /** Artistas que sigue (copia de solo lectura; puede estar vacía). */
    public synchronized List<Artista> getSeguidos() {
        return List.copyOf(seguidos);
    }

    // ---------- Patrón Observador ----------

    @Override
    public synchronized void actualizar(EventoObra evento) {
        avisos.add(Objects.requireNonNull(evento, "evento"));
    }

    // ---------- Avisos ----------

    /** Avisos que aún no ha visto, y los marca como vistos. */
    public synchronized List<EventoObra> recogerAvisosNuevos() {
        List<EventoObra> nuevos = List.copyOf(avisos.subList(avisosLeidos, avisos.size()));
        avisosLeidos = avisos.size();
        return nuevos;
    }

    public synchronized int contarAvisosNuevos() {
        return avisos.size() - avisosLeidos;
    }

    /** Todos los avisos recibidos, leídos o no. */
    public synchronized List<EventoObra> getHistorialAvisos() {
        return List.copyOf(avisos);
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}