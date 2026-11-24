package artalert.gui;

import artalert.artistas.CatalogoArtistas;
import artalert.artistas.Vigilante;
import artalert.fuentes.FuenteObras;
import artalert.usuarios.Usuario;

import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.util.List;

/** Ventana de ArtAlert. Pide el nombre del primer usuario y muestra el panel principal. */
public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal(List<FuenteObras> fuentes, CatalogoArtistas catalogo, Vigilante vigilante,
                            CargadorImagenes cargador, GeneradorNovedades generador, Usuario primerUsuario) {
        super("ArtAlert · Avisos de arte");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new PanelPrincipal(fuentes, catalogo, vigilante, cargador, generador, primerUsuario));
        setMinimumSize(new Dimension(1100, 680));
        setSize(1320, 820);
        setLocationRelativeTo(null);
    }

    /** Aspecto claro y limpio con FlatLaf si está en el pom; si no, el del sistema. Acento dorado. */
    public static void aplicarAspecto() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Component.arc", 12);
            UIManager.put("Component.focusColor", Estilo.DORADO);
            UIManager.put("Component.accentColor", Estilo.DORADO);
        } catch (Exception sinFlatLaf) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // se queda el aspecto por defecto de Java
            }
        }
        UIManager.put("OptionPane.background", Estilo.PANEL);
        UIManager.put("Panel.background", Estilo.PANEL);
        UIManager.put("ComboBox.selectionBackground", Estilo.SELECCION);
        UIManager.put("ComboBox.selectionForeground", Estilo.TINTA);
    }

    /** Pregunta el nombre al arrancar. Devuelve null si se cancela. */
    public static Usuario preguntarPrimerUsuario() {
        return DialogoNombre.bienvenida().map(Usuario::new).orElse(null);
    }
}