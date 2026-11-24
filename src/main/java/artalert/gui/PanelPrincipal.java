package artalert.gui;

import artalert.artistas.Artista;
import artalert.artistas.CatalogoArtistas;
import artalert.artistas.Vigilante;
import artalert.fuentes.FuenteObras;
import artalert.modelo.EventoObra;
import artalert.modelo.Obra;
import artalert.modelo.TipoEvento;
import artalert.usuarios.Usuario;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Pantalla principal de ArtAlert: mis artistas a la izquierda, la galería de obras en el centro
 * y los avisos del patrón Observador a la derecha.
 *
 * <p>Solo se encarga de mostrar y de recoger acciones (SRP); el trabajo lo hacen el catálogo,
 * el vigilante y el usuario, que recibe por constructor (DIP). Las llamadas a los museos se
 * hacen en segundo plano para que la ventana no se congele.
 */
public class PanelPrincipal extends JPanel {

    private final List<FuenteObras> fuentes;
    private final CatalogoArtistas catalogo;
    private final Vigilante vigilante;
    private final CargadorImagenes cargador;
    private final GeneradorNovedades generador;

    private final DefaultComboBoxModel<Usuario> usuarios = new DefaultComboBoxModel<>();
    private final DefaultListModel<Artista> modeloArtistas = new DefaultListModel<>();
    private final JList<Artista> listaArtistas = new JList<>(modeloArtistas);
    private final DefaultListModel<EventoObra> modeloAvisos = new DefaultListModel<>();
    private final JList<EventoObra> listaAvisos = new JList<>(modeloAvisos);
    private final JPanel galeria = new PanelGaleria();
    private final JLabel tituloGaleria = new JLabel();
    private final JLabel subtituloGaleria = new JLabel();
    private final JLabel tituloAvisos = Estilo.seccion("Avisos");
    private final JLabel estado = new JLabel(" ");
    private final List<JButton> botones = new ArrayList<>();
    private int avisosSinLeerMostrados = 0;

    /**
     * @param generador para el botón de demostración; si es null, el botón no aparece
     */
    public PanelPrincipal(List<FuenteObras> fuentes, CatalogoArtistas catalogo, Vigilante vigilante,
                          CargadorImagenes cargador, GeneradorNovedades generador, Usuario primerUsuario) {
        super(new BorderLayout());
        this.fuentes = List.copyOf(fuentes);
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
        this.vigilante = Objects.requireNonNull(vigilante, "vigilante");
        this.cargador = Objects.requireNonNull(cargador, "cargador");
        this.generador = generador;
        usuarios.addElement(Objects.requireNonNull(primerUsuario, "primerUsuario"));

        setBackground(Estilo.FONDO);
        add(crearCabecera(), BorderLayout.NORTH);
        add(crearColumnaArtistas(), BorderLayout.WEST);
        add(crearGaleria(), BorderLayout.CENTER);
        add(crearColumnaAvisos(), BorderLayout.EAST);
        add(crearBarraEstado(), BorderLayout.SOUTH);
        refrescarTodo();
    }

    // ---------- Construcción de la pantalla ----------

    private JComponent crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Estilo.NEGRO);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Estilo.DORADO),
                BorderFactory.createEmptyBorder(16, 28, 16, 28)));

        JPanel marca = new JPanel();
        marca.setLayout(new BoxLayout(marca, BoxLayout.Y_AXIS));
        marca.setOpaque(false);
        JLabel nombre = new JLabel("<html><span style='font-weight:normal'>Art</span>"
                + "<span style='color:" + Estilo.hex(Estilo.DORADO_CLARO) + "'>ALERT</span></html>");
        nombre.setFont(Estilo.TITULO);
        nombre.setForeground(Estilo.CREMA);
        JLabel lema = new JLabel("✦  EL PATRÓN OBSERVADOR APLICADO AL ARTE");
        lema.setFont(Estilo.SUBTITULO);
        lema.setForeground(Estilo.DORADO_CLARO);
        marca.add(nombre);
        marca.add(lema);
        cabecera.add(marca, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        acciones.setOpaque(false);
        JComboBox<Usuario> selector = new JComboBox<>(usuarios);
        selector.setFont(Estilo.NEGRITA);
        selector.setPreferredSize(new Dimension(170, 36));
        selector.addActionListener(e -> refrescarTodo());
        JButton nuevoUsuario = registrar(Estilo.botonSobreNegro("+ Usuario"));
        nuevoUsuario.addActionListener(e -> crearUsuario());
        acciones.add(etiqueta("Usuario:", Estilo.NORMAL, new Color(0xB7AC98)));
        acciones.add(selector);
        acciones.add(nuevoUsuario);
        if (generador != null) {
            JButton simular = registrar(Estilo.botonSobreNegro("✦ Simular novedad"));
            simular.setToolTipText("Demostración: provoca un cambio en el artista seleccionado");
            simular.addActionListener(e -> simularNovedad());
            acciones.add(simular);
        }
        JButton comprobar = registrar(Estilo.botonDorado("↻ Comprobar novedades"));
        comprobar.addActionListener(e -> comprobarNovedades());
        acciones.add(comprobar);
        cabecera.add(acciones, BorderLayout.EAST);
        return cabecera;
    }

    private JComponent crearColumnaArtistas() {
        JPanel columna = columnaLateral();
        columna.add(Estilo.seccion("Mis artistas"), BorderLayout.NORTH);

        listaArtistas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaArtistas.setCellRenderer(new RenderArtista());
        listaArtistas.setBackground(Estilo.PANEL);
        listaArtistas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarGaleria();
            }
        });
        columna.add(Estilo.scrollFino(new JScrollPane(listaArtistas)), BorderLayout.CENTER);

        JPanel pie = new JPanel(new GridLayout(2, 1, 0, 8));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        JButton seguir = registrar(Estilo.botonPrincipal("+ Seguir artista"));
        seguir.addActionListener(e -> seguirArtista());
        JButton dejar = registrar(Estilo.botonSecundario("Dejar de seguir"));
        dejar.addActionListener(e -> dejarDeSeguir());
        pie.add(seguir);
        pie.add(dejar);
        columna.add(pie, BorderLayout.SOUTH);
        return columna;
    }

    private JComponent crearGaleria() {
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Estilo.FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(22, 26, 10, 26));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);
        tituloGaleria.setFont(Estilo.TITULO.deriveFont(28f));
        tituloGaleria.setForeground(Estilo.TINTA);
        subtituloGaleria.setFont(Estilo.NORMAL);
        subtituloGaleria.setForeground(Estilo.TINTA_SUAVE);
        encabezado.add(tituloGaleria);
        encabezado.add(Box.createVerticalStrut(4));
        encabezado.add(subtituloGaleria);
        encabezado.setBorder(BorderFactory.createEmptyBorder(0, 6, 10, 0));
        centro.add(encabezado, BorderLayout.NORTH);

        galeria.setBackground(Estilo.FONDO);
        JScrollPane scroll = Estilo.scrollFino(new JScrollPane(galeria));
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        centro.add(scroll, BorderLayout.CENTER);
        return centro;
    }

    private JComponent crearColumnaAvisos() {
        JPanel columna = columnaLateral();
        columna.add(tituloAvisos, BorderLayout.NORTH);

        listaAvisos.setCellRenderer(new RenderAviso());
        listaAvisos.setBackground(Estilo.PANEL);
        listaAvisos.setFocusable(false);
        columna.add(Estilo.scrollFino(new JScrollPane(listaAvisos)), BorderLayout.CENTER);

        JButton leidos = registrar(Estilo.botonSecundario("Marcar como leídos"));
        leidos.addActionListener(e -> {
            usuarioActual().recogerAvisosNuevos();
            refrescarAvisos();
        });
        JPanel pie = new JPanel(new GridLayout(1, 1));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        pie.add(leidos);
        columna.add(pie, BorderLayout.SOUTH);
        return columna;
    }

    private JComponent crearBarraEstado() {
        estado.setFont(Estilo.PEQUENA);
        estado.setForeground(Estilo.TINTA_SUAVE);
        estado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE),
                BorderFactory.createEmptyBorder(6, 28, 6, 28)));
        estado.setOpaque(true);
        estado.setBackground(Estilo.FONDO);
        return estado;
    }

    // ---------- Acciones ----------

    private void seguirArtista() {
        DialogoSeguir.preguntar(SwingUtilities.getWindowAncestor(this), fuentes).ifPresent(eleccion -> {
            Usuario usuario = usuarioActual();
            enSegundoPlano("Buscando a " + eleccion.nombreArtista() + " en los museos…", () -> {
                List<String> mensajes = new ArrayList<>();
                for (FuenteObras fuente : eleccion.museos()) {
                    Artista artista = catalogo.obtener(eleccion.nombreArtista(), fuente);
                    if (artista.getObras().isEmpty()) {
                        vigilante.comprobar(artista).ifPresent(mensajes::add);
                    }
                    if (artista.getObras().isEmpty()) {
                        mensajes.add("No hay obras de " + eleccion.nombreArtista() + " en " + fuente.getNombreMuseo());
                    } else if (usuario.seguir(artista)) {
                        mensajes.add("Ahora sigues a " + artista);
                    }
                }
                return mensajes;
            }, () -> {
                refrescarArtistas();
                seleccionarUltimoArtista();
            });
        });
    }

    private void dejarDeSeguir() {
        Artista artista = listaArtistas.getSelectedValue();
        if (artista == null) {
            avisar("Elige primero un artista de la lista.");
            return;
        }
        usuarioActual().dejarDeSeguir(artista);
        avisar("Has dejado de seguir a " + artista + ".");
        refrescarArtistas();
    }

    private void comprobarNovedades() {
        int antes = usuarioActual().contarAvisosNuevos();
        enSegundoPlano("Consultando los museos…", vigilante::comprobar, () -> {
            int nuevos = usuarioActual().contarAvisosNuevos() - antes;
            refrescarTodo();
            avisar(nuevos > 0 ? "¡Tienes " + nuevos + " avisos nuevos!" : "No hay novedades en tus artistas.");
        });
    }

    private void simularNovedad() {
        Artista artista = listaArtistas.getSelectedValue();
        if (artista == null) {
            avisar("Elige primero un artista para simular una novedad.");
            return;
        }
        generador.prepararNovedad(artista);
        avisar("Novedad preparada en " + artista + ". Pulsa «Comprobar novedades».");
    }

    private void crearUsuario() {
        DialogoNombre.nuevoUsuario(SwingUtilities.getWindowAncestor(this)).ifPresent(nombre -> {
            Usuario usuario = new Usuario(nombre);
            usuarios.addElement(usuario);
            usuarios.setSelectedItem(usuario);
        });
    }

    /** Ejecuta una tarea lenta fuera del hilo de la ventana y muestra sus mensajes al acabar. */
    private void enSegundoPlano(String mensaje, Supplier<List<String>> tarea, Runnable despues) {
        botones.forEach(b -> b.setEnabled(false));
        avisar(mensaje);
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return tarea.get();
            }

            @Override
            protected void done() {
                botones.forEach(b -> b.setEnabled(true));
                despues.run();
                try {
                    List<String> mensajes = get();
                    if (!mensajes.isEmpty()) {
                        avisar(String.join("  ·  ", mensajes));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    avisar("Algo ha fallado: " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    // ---------- Refrescar la pantalla ----------

    private Usuario usuarioActual() {
        return (Usuario) usuarios.getSelectedItem();
    }

    private void refrescarTodo() {
        refrescarArtistas();
        refrescarAvisos();
    }

    private void refrescarArtistas() {
        Artista seleccionado = listaArtistas.getSelectedValue();
        modeloArtistas.clear();
        usuarioActual().getSeguidos().forEach(modeloArtistas::addElement);
        if (seleccionado != null && modeloArtistas.contains(seleccionado)) {
            listaArtistas.setSelectedValue(seleccionado, true);
        } else if (!modeloArtistas.isEmpty()) {
            listaArtistas.setSelectedIndex(0);
        }
        mostrarGaleria();
    }

    private void seleccionarUltimoArtista() {
        if (!modeloArtistas.isEmpty()) {
            listaArtistas.setSelectedIndex(modeloArtistas.size() - 1);
        }
    }

    private void mostrarGaleria() {
        galeria.removeAll();
        Artista artista = listaArtistas.getSelectedValue();
        if (artista == null) {
            tituloGaleria.setText("Bienvenida/o, " + usuarioActual().getNombre());
            subtituloGaleria.setText("Pulsa «+ Seguir artista» para empezar tu colección.");
        } else {
            List<Obra> obras = artista.getObras();
            tituloGaleria.setText(artista.getNombre());
            subtituloGaleria.setText(artista.getMuseo() + "  ·  " + resumen(obras));
            // Primero las obras con foto; las protegidas por derechos de autor, al final.
            obras.stream()
                    .sorted(Comparator.comparing((Obra obra) -> !obra.tieneImagen()))
                    .forEach(obra -> galeria.add(new TarjetaObra(obra, cargador)));
        }
        galeria.revalidate();
        galeria.repaint();
    }

    private void refrescarAvisos() {
        Usuario usuario = usuarioActual();
        List<EventoObra> historial = new ArrayList<>(usuario.getHistorialAvisos());
        Collections.reverse(historial);
        modeloAvisos.clear();
        historial.forEach(modeloAvisos::addElement);
        avisosSinLeerMostrados = usuario.contarAvisosNuevos();
        tituloAvisos.setText(avisosSinLeerMostrados > 0
                ? "AVISOS  ·  " + avisosSinLeerMostrados + " NUEVOS" : "AVISOS");
        tituloAvisos.setForeground(avisosSinLeerMostrados > 0 ? Estilo.DORADO : Estilo.TINTA_SUAVE);
        listaAvisos.repaint();
    }

    private void avisar(String mensaje) {
        estado.setText(mensaje);
    }

    // ---------- Ayudas ----------

    private JButton registrar(JButton boton) {
        botones.add(boton);
        return boton;
    }

    private static JPanel columnaLateral() {
        JPanel columna = new JPanel(new BorderLayout());
        columna.setBackground(Estilo.PANEL);
        columna.setPreferredSize(new Dimension(290, 0));
        columna.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 1, Estilo.BORDE),
                BorderFactory.createEmptyBorder(20, 18, 18, 18)));
        return columna;
    }

    private static JLabel etiqueta(String texto, java.awt.Font fuente, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente);
        etiqueta.setForeground(color);
        return etiqueta;
    }

    private static String resumen(List<Obra> obras) {
        long enSala = obras.stream().filter(Obra::enSala).count();
        return obras.size() + (obras.size() == 1 ? " obra" : " obras") + "  ·  " + enSala + " en sala";
    }

    /** Galería que ocupa el ancho visible y baja a la fila siguiente (solo scroll vertical). */
    private static class PanelGaleria extends JPanel implements Scrollable {
        PanelGaleria() {
            super(new DisposicionFluida(16));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return visible.height - 24;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Cómo se dibuja cada artista en la lista. */
    private static class RenderArtista extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                      boolean seleccionado, boolean foco) {
            Artista artista = (Artista) valor;
            JLabel celda = (JLabel) super.getListCellRendererComponent(lista, valor, indice, seleccionado, false);
            celda.setText("<html><b style='font-size:11px'>" + TarjetaObra.escapar(artista.getNombre()) + "</b><br>"
                    + "<span style='color:" + Estilo.hex(Estilo.TINTA_SUAVE) + "'>" + TarjetaObra.escapar(artista.getMuseo()) + "<br>"
                    + resumen(artista.getObras()) + "</span></html>");
            celda.setFont(Estilo.NORMAL);
            celda.setForeground(Estilo.TINTA);
            celda.setBackground(seleccionado ? Estilo.SELECCION : Estilo.PANEL);
            celda.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 1, 0, seleccionado ? Estilo.DORADO : Estilo.PANEL),
                    BorderFactory.createEmptyBorder(10, 10, 10, 6)));
            return celda;
        }
    }

    /** Cómo se dibuja cada aviso: color según el tipo y los no leídos resaltados. */
    private class RenderAviso extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                      boolean seleccionado, boolean foco) {
            EventoObra evento = (EventoObra) valor;
            JLabel celda = (JLabel) super.getListCellRendererComponent(lista, valor, indice, false, false);
            boolean sinLeer = indice < avisosSinLeerMostrados;
            String color = switch (evento.tipo()) {
                case NUEVA -> Estilo.hex(Estilo.DORADO);
                case EXPUESTA -> Estilo.hex(Estilo.VERDE);
                case RETIRADA -> Estilo.hex(Estilo.ROJO);
            };
            celda.setText("<html><span style='color:" + color + ";font-size:9px'><b>"
                    + etiquetaTipo(evento.tipo()) + "</b></span>" + (sinLeer ? "  <b style='color:" + Estilo.hex(Estilo.DORADO) + "'>•</b>" : "")
                    + "<br><span style='font-size:11px'>" + TarjetaObra.escapar(evento.obra().titulo()) + "</span><br>"
                    + "<span style='color:" + Estilo.hex(Estilo.TINTA_SUAVE) + ";font-size:9px'>" + TarjetaObra.escapar(evento.obra().artista())
                    + "  ·  " + TarjetaObra.escapar(evento.obra().museo()) + "</span></html>");
            celda.setFont(Estilo.NORMAL);
            celda.setForeground(Estilo.TINTA);
            celda.setBackground(sinLeer ? Estilo.SIN_LEER : Estilo.PANEL);
            celda.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                    BorderFactory.createEmptyBorder(10, 6, 10, 6)));
            return celda;
        }

        private String etiquetaTipo(TipoEvento tipo) {
            return switch (tipo) {
                case NUEVA -> "NUEVA OBRA";
                case EXPUESTA -> "AHORA EN SALA";
                case RETIRADA -> "RETIRADA DE SALA";
            };
        }
    }
}