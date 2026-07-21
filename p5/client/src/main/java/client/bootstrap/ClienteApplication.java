package client.bootstrap;

import client.ui.ConexionDialog;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class ClienteApplication {

    private ClienteApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            aplicarAparienciaDelSistema();
            new ConexionDialog(null, null, null).setVisible(true);
        });
    }

    private static void aplicarAparienciaDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
            // Swing conserva su apariencia multiplataforma predeterminada.
        }
    }
}
