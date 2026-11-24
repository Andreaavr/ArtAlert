package artalert.gui;

import artalert.artistas.Artista;
import artalert.modelo.Obra;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Component;
import java.util.List;

/** Cómo se dibuja cada artista en la lista «Mis artistas»: nombre, museo y resumen de obras. */
class RenderArtista extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                  boolean seleccionado, boolean foco) {
        Artista artista = (Artista) valor;
        JLabel celda = (JLabel) super.getListCellRendererComponent(lista, valor, indice, seleccionado, false);
        celda.setText("<html><b style='font-size:11px'>" + Estilo.escapar(artista.getNombre()) + "</b><br>"
                + "<span style='color:" + Estilo.hex(Estilo.TINTA_SUAVE) + "'>"
                + Estilo.escapar(artista.getMuseo()) + "<br>" + resumen(artista.getObras()) + "</span></html>");
        celda.setFont(Estilo.NORMAL);
        celda.setForeground(Estilo.TINTA);
        celda.setBackground(seleccionado ? Estilo.SELECCION : Estilo.PANEL);
        celda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 1, 0, seleccionado ? Estilo.DORADO : Estilo.PANEL),
                BorderFactory.createEmptyBorder(10, 10, 10, 6)));
        return celda;
    }

    /** "47 obras · 28 en sala". */
    static String resumen(List<Obra> obras) {
        long enSala = obras.stream().filter(Obra::enSala).count();
        return obras.size() + (obras.size() == 1 ? " obra" : " obras") + "  ·  " + enSala + " en sala";
    }
}