package artalert.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * FlowLayout que, dentro de un panel con scroll, salta de línea al llegar al borde.
 * Así la galería coloca tantas obras por fila como quepan en la ventana.
 */
public class DisposicionFluida extends FlowLayout {

    public DisposicionFluida(int separacion) {
        super(FlowLayout.LEFT, separacion, separacion);
    }

    @Override
    public Dimension preferredLayoutSize(Container destino) {
        return calcular(destino);
    }

    @Override
    public Dimension minimumLayoutSize(Container destino) {
        return calcular(destino);
    }

    private Dimension calcular(Container destino) {
        synchronized (destino.getTreeLock()) {
            int anchoDisponible = destino.getSize().width;
            Container padre = SwingUtilities.getAncestorOfClass(JScrollPane.class, destino);
            if (padre instanceof JScrollPane scroll) {
                anchoDisponible = scroll.getViewport().getWidth();
            }
            if (anchoDisponible <= 0) {
                anchoDisponible = Integer.MAX_VALUE;
            }
            Insets bordes = destino.getInsets();
            int maximo = anchoDisponible - bordes.left - bordes.right - getHgap() * 2;
            int ancho = 0;
            int alto = 0;
            int anchoFila = 0;
            int altoFila = 0;
            for (Component componente : destino.getComponents()) {
                if (!componente.isVisible()) {
                    continue;
                }
                Dimension d = componente.getPreferredSize();
                if (anchoFila + d.width > maximo && anchoFila > 0) {
                    ancho = Math.max(ancho, anchoFila);
                    alto += altoFila + getVgap();
                    anchoFila = 0;
                    altoFila = 0;
                }
                anchoFila += d.width + getHgap();
                altoFila = Math.max(altoFila, d.height);
            }
            ancho = Math.max(ancho, anchoFila);
            alto += altoFila;
            return new Dimension(ancho + bordes.left + bordes.right + getHgap() * 2,
                    alto + bordes.top + bordes.bottom + getVgap() * 2);
        }
    }
}
