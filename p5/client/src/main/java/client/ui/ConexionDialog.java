package client.ui;

import client.rmi.ConexionRmi;

import javax.swing.*;
import java.awt.*;
import java.rmi.NotBoundException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class ConexionDialog extends JDialog {

    public static final int PUERTO_PREDETERMINADO = 1099;
    public static final String HOST_PREDETERMINADO = "localhost";

    public static final String SERVICIO_PREDETERMINADO = "TableroNoticias";

    private final JTextField servidorField = new JTextField(20);
    private final JTextField puertoField = new JTextField(8);
    private final JTextField servicioField = new JTextField(20);
    private final JButton conectarButton = new JButton("Conectar");
    private final JButton salirButton = new JButton("Salir");
    private final JLabel estadoLabel = new JLabel("Introduzca los datos del servidor.");
    private final Consumer<ConexionRmi> alConectar;

    public ConexionDialog(Window owner, ConexionRmi anterior,
                          Consumer<ConexionRmi> alConectar
    ) {
        super(owner, "Conexión al servidor", ModalityType.APPLICATION_MODAL);
        this.alConectar = alConectar;
        servidorField.setText(anterior == null ? HOST_PREDETERMINADO : anterior.getHost());
        puertoField.setText(String.valueOf(anterior == null ? PUERTO_PREDETERMINADO : anterior.getPuerto()));
        servicioField.setText(anterior == null ? SERVICIO_PREDETERMINADO : anterior.getServicioNombre());

        construirInterfaz();
    }

    private void construirInterfaz() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        agregarCampo(formulario, c, 0, "Servidor:", servidorField);
        agregarCampo(formulario, c, 1, "Puerto:", puertoField);
        agregarCampo(formulario, c, 2, "Servicio:", servicioField);

        JPanel acciones = new JPanel();
        acciones.add(conectarButton);
        acciones.add(salirButton);

        JPanel pie = new JPanel(new BorderLayout());
        pie.add(estadoLabel, BorderLayout.CENTER);
        pie.add(acciones, BorderLayout.SOUTH);

        add(formulario, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
        conectarButton.addActionListener(event -> conectar());
        salirButton.addActionListener(event -> dispose());
        getRootPane().setDefaultButton(conectarButton);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private void agregarCampo(JPanel panel, GridBagConstraints c, int fila,
                              String etiqueta, JTextField campo) {
        c.gridx = 0;
        c.gridy = fila;
        c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(campo, c);
    }

    private void conectar() {
        String host = servidorField.getText().trim();
        String nombre = servicioField.getText().trim();
        int puerto;
        try {
            puerto = Integer.parseInt(puertoField.getText().trim());
            if (host.isEmpty() || nombre.isEmpty() || puerto < 1 || puerto > 65_535) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            mostrarError("Indique un servidor, un puerto válido y el nombre del servicio.");
            return;
        }

        cambiarOcupado(true, "Conectando...");
        new SwingWorker<ConexionRmi, Void>() {
            @Override
            protected ConexionRmi doInBackground() throws Exception {
                return ConexionRmi.conectar(host, puerto, nombre);
            }

            @Override
            protected void done() {
                try {
                    ConexionRmi conexion = get();
                    dispose();
                    if (alConectar == null) {
                        new MainFrame(conexion).setVisible(true);
                    } else {
                        alConectar.accept(conexion);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    cambiarOcupado(false, "Conexión interrumpida.");
                } catch (ExecutionException ex) {
                    String mensaje = mensajeConexion(ex.getCause());
                    cambiarOcupado(false, mensaje);
                    JOptionPane.showMessageDialog(ConexionDialog.this, mensaje,
                            "Conexión fallida", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String mensajeConexion(Throwable error) {
        if (error instanceof NotBoundException) {
            return "El servicio indicado no está registrado en el servidor.";
        }
        return "No fue posible conectar. Revise dirección, puertos, firewall y servicio.";
    }

    private void cambiarOcupado(boolean ocupado, String mensaje) {
        conectarButton.setEnabled(!ocupado);
        salirButton.setEnabled(!ocupado);
        servidorField.setEnabled(!ocupado);
        puertoField.setEnabled(!ocupado);
        servicioField.setEnabled(!ocupado);
        setCursor(ocupado
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor());
        estadoLabel.setText(mensaje);
    }

    private void mostrarError(String mensaje) {
        estadoLabel.setText(mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "Datos inválidos",
                JOptionPane.WARNING_MESSAGE);
    }
}
