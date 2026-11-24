package artalert.gui;

import artalert.modelo.Obra;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Tarjeta de la galería: la imagen de la obra con su cartela debajo, como en un museo. */
public class TarjetaObra extends JPanel {

    static final int ANCHO_IMAGEN = 210;
    static final int ALTO_IMAGEN = 170;

    public TarjetaObra(Obra obra, CargadorImagenes cargador) {
        super(new BorderLayout(0, 8));
        setOpaque(false);
        setBorder(Estilo.margen(12));
        setPreferredSize(new Dimension(ANCHO_IMAGEN + 24, ALTO_IMAGEN + 118));

        JLabel imagen = new JLabel("<html><div style='text-align:center'>Imagen protegida<br>por derechos de autor"
                + "</div></html>", SwingConstants.CENTER);
        imagen.setPreferredSize(new Dimension(ANCHO_IMAGEN, ALTO_IMAGEN));
        imagen.setOpaque(true);
        imagen.setBackground(Estilo.HUECO);
        imagen.setForeground(Estilo.TINTA_SUAVE);
        imagen.setFont(Estilo.PEQUENA);
        imagen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 4, 4, 4, Estilo.DORADO),
                BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(0x6E5626))));
        if (obra.tieneImagen()) {
            imagen.setText("Cargando…");
            cargador.cargar(obra.imagenUrl(), ANCHO_IMAGEN - 12, ALTO_IMAGEN - 12, icono -> {
                if (icono == null) {
                    imagen.setText("Imagen no disponible");
                } else {
                    imagen.setText(null);
                    imagen.setIcon(icono);
                }
            });
        }
        add(imagen, BorderLayout.NORTH);

        JLabel titulo = new JLabel("<html><div style='width:" + (ANCHO_IMAGEN - 4) + "px'>"
                + Estilo.escapar(obra.titulo()) + "</div></html>");
        titulo.setFont(Estilo.OBRA);
        titulo.setForeground(Estilo.TINTA);
        titulo.setVerticalAlignment(SwingConstants.TOP);
        add(titulo, BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        pie.add(obra.enSala()
                ? new Etiqueta("● EN SALA", Estilo.VERDE)
                : new Etiqueta("EN ALMACÉN", Estilo.TINTA_SUAVE));
        add(pie, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Estilo.PANEL);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.setColor(Estilo.BORDE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }

    /** Pequeña etiqueta redondeada de color. */
    static class Etiqueta extends JLabel {
        private final Color color;

        Etiqueta(String texto, Color color) {
            super(texto);
            this.color = color;
            setFont(Estilo.SECCION.deriveFont(10f));
            setForeground(color);
            setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}