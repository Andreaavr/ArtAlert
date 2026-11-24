package artalert.gui;

import artalert.fuentes.FuenteObras;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Ventana para elegir a qué artista seguir y en qué museos (uno, varios o todos). */
public class DialogoSeguir extends JDialog {

    /** Lo que ha elegido el usuario. */
    public record Seleccion(String nombreArtista, List<FuenteObras> museos) {
    }

    private final JTextField campoNombre = new JTextField(22);
    private final Map<JCheckBox, FuenteObras> casillas = new LinkedHashMap<>();
    private final JLabel error = new JLabel(" ");
    private Seleccion seleccion;

    private DialogoSeguir(Window padre, List<FuenteObras> fuentes) {
        super(padre, "Seguir a un artista", ModalityType.APPLICATION_MODAL);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Estilo.PANEL);
        contenido.setBorder(Estilo.margen(24));

        JLabel titulo = new JLabel("¿A quién quieres seguir?");
        titulo.setFont(Estilo.TITULO.deriveFont(24f));
        titulo.setForeground(Estilo.TINTA);
        contenido.add(alinear(titulo));
        contenido.add(Box.createVerticalStrut(16));

        contenido.add(alinear(Estilo.seccion("Artista")));
        campoNombre.setFont(Estilo.NORMAL.deriveFont(15f));
        campoNombre.setBorder(Estilo.tarjeta(8));
        campoNombre.setBackground(Color.WHITE);
        campoNombre.setForeground(Estilo.TINTA);
        campoNombre.setCaretColor(Estilo.DORADO);
        contenido.add(alinear(campoNombre));
        contenido.add(Box.createVerticalStrut(18));

        contenido.add(alinear(Estilo.seccion("Museos")));
        for (FuenteObras fuente : fuentes) {
            JCheckBox casilla = new JCheckBox(fuente.getNombreMuseo(), true);
            casilla.setFont(Estilo.NORMAL);
            casilla.setOpaque(false);
            casilla.setForeground(Estilo.TINTA);
            casilla.setFocusPainted(false);
            casillas.put(casilla, fuente);
            contenido.add(alinear(casilla));
        }

        error.setFont(Estilo.PEQUENA);
        error.setForeground(Estilo.ROJO);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(alinear(error));

        JButton cancelar = Estilo.botonSecundario("Cancelar");
        JButton aceptar = Estilo.botonPrincipal("Seguir");
        cancelar.addActionListener(e -> dispose());
        aceptar.addActionListener(e -> aceptar());
        campoNombre.addActionListener(e -> aceptar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setBackground(Estilo.PANEL);
        botones.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));
        botones.add(cancelar);
        botones.add(aceptar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contenido, BorderLayout.CENTER);
        getContentPane().add(botones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(aceptar);
        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    /** Abre la ventana y espera. Vacío si el usuario cancela. */
    public static Optional<Seleccion> preguntar(Window padre, List<FuenteObras> fuentes) {
        DialogoSeguir dialogo = new DialogoSeguir(padre, fuentes);
        dialogo.setVisible(true);
        return Optional.ofNullable(dialogo.seleccion);
    }

    private void aceptar() {
        String nombre = campoNombre.getText().trim();
        List<FuenteObras> elegidos = new ArrayList<>();
        casillas.forEach((casilla, fuente) -> {
            if (casilla.isSelected()) {
                elegidos.add(fuente);
            }
        });
        if (nombre.isEmpty()) {
            error.setText("Escribe el nombre del artista.");
        } else if (elegidos.isEmpty()) {
            error.setText("Elige al menos un museo.");
        } else {
            seleccion = new Seleccion(nombre, List.copyOf(elegidos));
            dispose();
        }
    }

    private static Component alinear(javax.swing.JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        return componente;
    }
}
