package client.ui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

public final class LoginDialog extends JDialog {

    private final JTextField usuarioField = new JTextField(20);
    private final JPasswordField contrasenaField = new JPasswordField(20);
    private Credenciales resultado;

    private LoginDialog(Window owner) {
        super(owner, "Iniciar sesión", ModalityType.APPLICATION_MODAL);
        construirInterfaz();
    }

    public static Credenciales solicitar(Window owner) {
        LoginDialog dialog = new LoginDialog(owner);
        dialog.setVisible(true);
        return dialog.resultado;
    }

    private void construirInterfaz() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        formulario.add(new JLabel("Usuario:"), c);
        c.gridx = 1;
        formulario.add(usuarioField, c);
        c.gridx = 0;
        c.gridy = 1;
        formulario.add(new JLabel("Contraseña:"), c);
        c.gridx = 1;
        formulario.add(contrasenaField, c);

        JButton iniciar = new JButton("Iniciar sesión");
        JButton cancelar = new JButton("Cancelar");
        JPanel acciones = new JPanel();
        acciones.add(iniciar);
        acciones.add(cancelar);
        add(formulario, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);

        iniciar.addActionListener(event -> aceptar());
        cancelar.addActionListener(event -> dispose());
        getRootPane().setDefaultButton(iniciar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private void aceptar() {
        String usuario = usuarioField.getText().trim();
        char[] contrasena = contrasenaField.getPassword();
        if (usuario.isEmpty() || contrasena.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Usuario y contraseña son obligatorios.", "Datos inválidos",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            java.util.Arrays.fill(contrasena, '\0');
            return;
        }
        resultado = new Credenciales(usuario, contrasena);
        dispose();
    }

    public record Credenciales(String usuario, char[] contrasena) {
    }
}
