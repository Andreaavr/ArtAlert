package artalert.gui;

import artalert.modelo.EventoObra;
import artalert.modelo.TipoEvento;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Color;
import java.awt.Component;
import java.util.function.IntSupplier;

/**
 * Cómo se dibuja cada aviso del Observador: un color por tipo de evento y los no leídos resaltados.
 * Los avisos van del más reciente al más antiguo, así que los primeros N son los no leídos.
 */
class RenderAviso extends DefaultListCellRenderer {

    private final IntSupplier cuantosSinLeer;

    /** @param cuantosSinLeer cuántos avisos del principio de la lista están sin leer */
    RenderAviso(IntSupplier cuantosSinLeer) {
        this.cuantosSinLeer = cuantosSinLeer;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                  boolean seleccionado, boolean foco) {
        EventoObra evento = (EventoObra) valor;
        JLabel celda = (JLabel) super.getListCellRendererComponent(lista, valor, indice, false, false);
        boolean sinLeer = indice < cuantosSinLeer.getAsInt();
        celda.setText("<html><span style='color:" + Estilo.hex(color(evento.tipo())) + ";font-size:9px'><b>"
                + etiqueta(evento.tipo()) + "</b></span>"
                + (sinLeer ? "  <b style='color:" + Estilo.hex(Estilo.DORADO) + "'>•</b>" : "")
                + "<br><span style='font-size:11px'>" + Estilo.escapar(evento.obra().titulo()) + "</span><br>"
                + "<span style='color:" + Estilo.hex(Estilo.TINTA_SUAVE) + ";font-size:9px'>"
                + Estilo.escapar(evento.obra().artista()) + "  ·  " + Estilo.escapar(evento.obra().museo())
                + "</span></html>");
        celda.setFont(Estilo.NORMAL);
        celda.setForeground(Estilo.TINTA);
        celda.setBackground(sinLeer ? Estilo.SIN_LEER : Estilo.PANEL);
        celda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                BorderFactory.createEmptyBorder(10, 6, 10, 6)));
        return celda;
    }

    private static Color color(TipoEvento tipo) {
        return switch (tipo) {
            case NUEVA -> Estilo.DORADO;
            case EXPUESTA -> Estilo.VERDE;
            case RETIRADA -> Estilo.ROJO;
        };
    }

    private static String etiqueta(TipoEvento tipo) {
        return switch (tipo) {
            case NUEVA -> "NUEVA OBRA";
            case EXPUESTA -> "AHORA EN SALA";
            case RETIRADA -> "RETIRADA DE SALA";
        };
    }
}
