package artalert.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Colores, tipos de letra y piezas comunes de la interfaz, en un solo sitio.
 * Si se quiere cambiar el aspecto, se cambia aquí y afecta a toda la aplicación.
 */
public final class Estilo {

    private Estilo() {
    }

    // Paleta de la presentación: marfil de fondo, negro como acento (cabecera, botones, marcos) y dorado.
    public static final Color FONDO = new Color(0xF3EDE1);
    public static final Color PANEL = new Color(0xFBF8F2);
    public static final Color NEGRO = new Color(0x161513);
    public static final Color CREMA = new Color(0xEFE6D6);
    public static final Color HUECO = new Color(0x22201C);
    public static final Color BORDE = new Color(0xE3D9C6);
    public static final Color TINTA = new Color(0x1C1A17);
    public static final Color TINTA_SUAVE = new Color(0x7D7466);
    public static final Color DORADO = new Color(0xB8913F);
    public static final Color DORADO_CLARO = new Color(0xD8B96A);
    public static final Color VERDE = new Color(0x4F7A5A);
    public static final Color ROJO = new Color(0x8E3B3B);
    public static final Color SELECCION = new Color(0xF2E8D2);
    public static final Color SIN_LEER = new Color(0xF8F0DE);

    public static final Font TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 32);
    public static final Font SUBTITULO = new Font(Font.SERIF, Font.PLAIN, 12);
    public static final Font SECCION = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font NEGRITA = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font PEQUENA = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    public static final Font OBRA = new Font(Font.SERIF, Font.PLAIN, 14);

    /** Color en formato #RRGGBB, para el texto HTML de las listas. */
    public static String hex(Color color) {
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    /** Título de sección dorado en mayúsculas, como en la presentación. */
    public static JLabel seccion(String texto) {
        JLabel etiqueta = new JLabel(texto.toUpperCase());
        etiqueta.setFont(SECCION);
        etiqueta.setForeground(DORADO);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 2, 10, 0));
        return etiqueta;
    }

    /** Botón principal: negro con letra crema. */
    public static JButton botonPrincipal(String texto) {
        return boton(texto, NEGRO, CREMA, NEGRO);
    }

    /** Botón secundario: claro, solo con borde. */
    public static JButton botonSecundario(String texto) {
        return boton(texto, PANEL, TINTA, new Color(0xCFC2A8));
    }

    /** Botón dorado, para la acción más importante sobre la cabecera negra. */
    public static JButton botonDorado(String texto) {
        return boton(texto, DORADO, NEGRO, DORADO);
    }

    /** Botón con borde dorado y fondo transparente, para la cabecera negra. */
    public static JButton botonSobreNegro(String texto) {
        return boton(texto, new Color(0x262420), CREMA, new Color(0x6B5A35));
    }

    private static JButton boton(String texto, Color fondo, Color letra, Color borde) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color relleno = getModel().isRollover() ? mezclar(fondo, DORADO, 0.18f) : fondo;
                g2.setColor(isEnabled() ? relleno : BORDE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(borde);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setFont(NEGRITA.deriveFont(13f));
        boton.setForeground(letra);
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        return boton;
    }

    private static Color mezclar(Color a, Color b, float cantidad) {
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * cantidad),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * cantidad),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * cantidad));
    }

    /** Quita el borde y pinta las barras de desplazamiento finas. */
    public static <T extends JScrollPane> T scrollFino(T scroll) {
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUI(new BarraFina());
        scroll.getHorizontalScrollBar().setUI(new BarraFina());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setOpaque(false);
        return scroll;
    }

    /** Barra de desplazamiento sin flechas, con el tirador redondeado. */
    private static class BarraFina extends BasicScrollBarUI {
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle zona) {
            // pista invisible
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle zona) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? DORADO : new Color(0xD3C7B0));
            g2.fillRoundRect(zona.x + 1, zona.y, zona.width - 2, zona.height, zona.width, zona.width);
            g2.dispose();
        }

        @Override
        protected JButton createDecreaseButton(int orientacion) {
            return sinFlecha();
        }

        @Override
        protected JButton createIncreaseButton(int orientacion) {
            return sinFlecha();
        }

        private static JButton sinFlecha() {
            JButton nada = new JButton();
            nada.setPreferredSize(new Dimension(0, 0));
            return nada;
        }
    }

    /** Margen interior uniforme. */
    public static Border margen(int px) {
        return BorderFactory.createEmptyBorder(px, px, px, px);
    }

    /** Borde fino de color con margen interior. */
    public static Border tarjeta(int px) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true), margen(px));
    }
}
