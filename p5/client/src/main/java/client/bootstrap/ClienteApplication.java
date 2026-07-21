package client.bootstrap;

import client.rmi.ConexionRmi;
import client.ui.ConexionDialog;
import client.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public final class ClienteApplication {

    private ClienteApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            aplicarAparienciaDelSistema();
            //new ConexionDialog(null, null, null).setVisible(true);
            iniciar();
        });
    }

    private static void aplicarAparienciaDelSistema() {
        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            FlatLightLaf.setup();
        } catch (Exception ignored) {
            // Si FlatLaf no está disponible, Swing usa su default (Metal)
            // Swing conserva su apariencia multiplataforma predeterminada.
        }
    }

    private static void iniciar() {
        // Ventana de carga temporal mientras se intenta la conexión automática
        JWindow carga = new JWindow();
        JLabel etiqueta = new JLabel("Conectando al servidor…", SwingConstants.CENTER);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.PLAIN, 14f));
        etiqueta.setPreferredSize(new Dimension(280, 70));
        carga.add(etiqueta);
        carga.pack();
        carga.setLocationRelativeTo(null);
        carga.setVisible(true);

        new SwingWorker<ConexionRmi, Void>() {
            @Override
            protected ConexionRmi doInBackground() throws Exception {
                return ConexionRmi.conectar(
                        ConexionDialog.HOST_PREDETERMINADO,
                        ConexionDialog.PUERTO_PREDETERMINADO,
                        ConexionDialog.SERVICIO_PREDETERMINADO);
            }

            @Override
            protected void done() {
                carga.dispose(); // Cierra la ventana de carga siempre
                try {
                    ConexionRmi conexion = get();
                    new MainFrame(conexion).setVisible(true); // Conexión exitosa: abre directamente
                } catch (Exception ex) {
                    new ConexionDialog(null, null, null).setVisible(true); // Falló: muestra el diálogo
                }
            }
        }.execute();
    }
}