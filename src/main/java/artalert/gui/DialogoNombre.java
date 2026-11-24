package artalert.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Optional;

/**
 * Ventana para pedir un nombre de usuario, con el estilo de la aplicación:
 * franja negra con el logotipo arriba y cuerpo marfil. Se usa al arrancar y en «+ Usuario».
 */
public class DialogoNombre extends JDialog {

    private final JTextField campo = new JTextField(20);
    private final JLabel error = new JLabel(" ");
    private String nombre;

    private DialogoNombre(Window padre, String titulo, String pregunta, String textoAceptar, String textoCancelar) {
        super(padre, titulo, ModalityType.APPLICATION_MODAL);

        // Franja negra con el logotipo, como la cabecera de la app.
        JPanel franja = new JPanel();
        franja.setLayout(new BoxLayout(franja, BoxLayout.Y_AXIS));
        franja.setBackground(Estilo.NEGRO);
        franja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Estilo.DORADO),
                BorderFactory.createEmptyBorder(26, 32, 22, 32)));
        JLabel logo = new JLabel("<html><span style='font-weight:normal'>Art</span>"
                + "<span style='color:" + Estilo.hex(Estilo.DORADO_CLARO) + "'>ALERT</span></html>");
        logo.setFont(Estilo.TITULO.deriveFont(38f));
        logo.setForeground(Estilo.CREMA);
        JLabel lema = new JLabel("✦  EL PATRÓN OBSERVADOR APLICADO AL ARTE");
        lema.setFont(Estilo.SUBTITULO);
        lema.setForeground(Estilo.DORADO_CLARO);
        franja.add(alinear(logo));
        franja.add(Box.createVerticalStrut(4));
        franja.add(alinear(lema));

        // Cuerpo marfil con la pregunta.
        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBackground(Estilo.PANEL);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(24, 32, 8, 32));
        JLabel textoPregunta = new JLabel(pregunta);
        textoPregunta.setFont(Estilo.TITULO.deriveFont(20f));
        textoPregunta.setForeground(Estilo.TINTA);
        cuerpo.add(alinear(textoPregunta));
        cuerpo.add(Box.createVerticalStrut(14));
        cuerpo.add(alinear(Estilo.seccion("Tu nombre")));
        campo.setFont(Estilo.NORMAL.deriveFont(15f));
        campo.setBackground(Color.WHITE);
        campo.setForeground(Estilo.TINTA);
        campo.setCaretColor(Estilo.DORADO);
        campo.setBorder(Estilo.tarjeta(9));
        cuerpo.add(alinear(campo));
        error.setFont(Estilo.PEQUENA);
        error.setForeground(Estilo.ROJO);
        cuerpo.add(Box.createVerticalStrut(6));
        cuerpo.add(alinear(error));

        JButton cancelar = Estilo.botonSecundario(textoCancelar);
        JButton aceptar = Estilo.botonPrincipal(textoAceptar);
        cancelar.addActionListener(e -> dispose());
        aceptar.addActionListener(e -> aceptar());
        campo.addActionListener(e -> aceptar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setBackground(Estilo.PANEL);
        botones.setBorder(BorderFactory.createEmptyBorder(4, 32, 24, 32));
        botones.add(cancelar);
        botones.add(aceptar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(franja, BorderLayout.NORTH);
        getContentPane().add(cuerpo, BorderLayout.CENTER);
        getContentPane().add(botones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(aceptar);
        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    /** Pantalla de bienvenida al arrancar. Vacío si se cierra sin escribir nombre. */
    public static Optional<String> bienvenida() {
        return preguntar(null, "Bienvenida/o a ArtAlert", "¡Hola! ¿Cómo te llamas?", "Entrar", "Salir");
    }

    /** Pide el nombre de un usuario nuevo. Vacío si se cancela. */
    public static Optional<String> nuevoUsuario(Window padre) {
        return preguntar(padre, "Nuevo usuario", "¿Quién más va a seguir artistas?", "Crear", "Cancelar");
    }

    private static Optional<String> preguntar(Window padre, String titulo, String pregunta,
                                              String aceptar, String cancelar) {
        DialogoNombre dialogo = new DialogoNombre(padre, titulo, pregunta, aceptar, cancelar);
        dialogo.setVisible(true);
        return Optional.ofNullable(dialogo.nombre);
    }

    private void aceptar() {
        String escrito = campo.getText().trim();
        if (escrito.isEmpty()) {
            error.setText("Escribe un nombre para continuar.");
        } else {
            nombre = escrito;
            dispose();
        }
    }

    private static Component alinear(JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        return componente;
    }
}
