package client.ui;

import client.rmi.ConexionRmi;
import client.ui.model.NoticiaTableModel;
import common.exception.AutenticacionException;
import common.exception.ConflictoEdicionException;
import common.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class MainFrame extends JFrame {

    private final NoticiaTableModel tableModel = new NoticiaTableModel();
    private final JTable tabla = new JTable(tableModel);
    private final JTextField buscarField = new JTextField(22);
    private final JComboBox<Object> categoriaCombo = new JComboBox<>();
    private final JButton buscarButton = new JButton("Buscar");
    private final JButton limpiarButton = new JButton("Limpiar");
    private final JButton actualizarButton = new JButton("Actualizar");
    private final JButton verButton = new JButton("Ver noticia");
    private final JButton loginButton = new JButton("Iniciar sesión");
    private final JButton publicarButton = new JButton("Publicar");
    private final JButton editarButton = new JButton("Editar");
    private final JButton eliminarButton = new JButton("Eliminar");
    private final JButton logoutButton = new JButton("Cerrar sesión");
    private final JButton reconectarButton = new JButton("Reconectar");
    private final JLabel usuarioLabel = new JLabel("Modo lector");
    private final JLabel estadoLabel = new JLabel();
    private final JPanel accionesRedactor = new JPanel(new FlowLayout(FlowLayout.LEFT));

    private ConexionRmi conexion;
    private Sesion sesion;
    private long generacionConsulta;

    public MainFrame(ConexionRmi conexion) {
        super("Tablero distribuido de noticias");
        this.conexion = conexion;
        construirInterfaz();
        actualizarEstadoSesion();
        actualizarNoticias();
    }

    private void construirInterfaz() {
        JLabel titulo = new JLabel("TABLERO DISTRIBUIDO DE NOTICIAS");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 19f));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 10));

        categoriaCombo.addItem("Todas");
        for (Categoria categoria : Categoria.values()) {
            categoriaCombo.addItem(categoria);
        }
        categoriaCombo.setRenderer((lista, valor, indice, seleccionado, foco) -> {
            JLabel label = new JLabel(nombreCategoria(valor));
            label.setOpaque(true);
            label.setBackground(seleccionado ? lista.getSelectionBackground() : lista.getBackground());
            label.setForeground(seleccionado ? lista.getSelectionForeground() : lista.getForeground());
            label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            return label;
        });

        JPanel filtrosPrimeraFila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtrosPrimeraFila.add(new JLabel("Buscar:"));
        filtrosPrimeraFila.add(buscarField);
        filtrosPrimeraFila.add(buscarButton);
        filtrosPrimeraFila.add(limpiarButton);

        JPanel filtrosSegundaFila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtrosSegundaFila.add(new JLabel("Categoría:"));
        filtrosSegundaFila.add(categoriaCombo);
        filtrosSegundaFila.add(actualizarButton);

        JPanel filtros = new JPanel(new GridLayout(0, 1));
        filtros.add(filtrosPrimeraFila);
        filtros.add(filtrosSegundaFila);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo, BorderLayout.NORTH);
        norte.add(filtros, BorderLayout.CENTER);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabla.getSelectionModel().addListSelectionListener(event -> actualizarAcciones());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && tabla.getSelectedRow() >= 0) {
                    verNoticia();
                }
            }
        });

        JPanel accionesLectura = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accionesLectura.add(verButton);
        accionesLectura.add(loginButton);
        accionesLectura.add(usuarioLabel);

        accionesRedactor.add(publicarButton);
        accionesRedactor.add(editarButton);
        accionesRedactor.add(eliminarButton);
        accionesRedactor.add(logoutButton);

        JPanel acciones = new JPanel(new GridLayout(0, 1));
        acciones.add(accionesLectura);
        acciones.add(accionesRedactor);

        JPanel estado = new JPanel(new BorderLayout());
        estado.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));
        estado.add(estadoLabel, BorderLayout.CENTER);
        estado.add(reconectarButton, BorderLayout.EAST);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(acciones, BorderLayout.CENTER);
        sur.add(estado, BorderLayout.SOUTH);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        buscarButton.addActionListener(event -> actualizarNoticias());
        buscarField.addActionListener(event -> actualizarNoticias());
        categoriaCombo.addActionListener(event -> actualizarNoticias());
        limpiarButton.addActionListener(event -> limpiarFiltros());
        actualizarButton.addActionListener(event -> actualizarNoticias());
        verButton.addActionListener(event -> verNoticia());
        loginButton.addActionListener(event -> iniciarSesion());
        publicarButton.addActionListener(event -> publicarNoticia());
        editarButton.addActionListener(event -> editarNoticia());
        eliminarButton.addActionListener(event -> eliminarNoticia());
        logoutButton.addActionListener(event -> cerrarSesion());
        reconectarButton.addActionListener(event -> mostrarReconexion());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1040, 680);
        setLocationRelativeTo(null);
    }

    private void actualizarNoticias() {
        String texto = buscarField.getText().trim();
        Categoria categoria = categoriaSeleccionada();
        long generacion = ++generacionConsulta;
        ejecutar(() -> {
            List<Noticia> resultado;
            if (!texto.isEmpty()) {
                resultado = conexion.getServicio().buscarPorPalabraClave(texto);
                if (categoria != null) {
                    resultado = resultado.stream()
                            .filter(noticia -> categoria == noticia.categoria())
                            .toList();
                }
            } else if (categoria != null) {
                resultado = conexion.getServicio().buscarPorCategoria(categoria);
            } else {
                resultado = conexion.getServicio().listarNoticias();
            }
            return resultado;
        }, noticias -> {
            if (generacion != generacionConsulta) {
                return;
            }
            tableModel.setNoticias(noticias);
            tabla.clearSelection();
            estadoLabel.setText(noticias.isEmpty()
                    ? "Conectado. No se encontraron noticias."
                    : "Conectado a " + conexion.descripcion() + " — "
                    + noticias.size() + " noticia(s).");
        }, error -> {
            if (generacion == generacionConsulta) {
                manejarFallo(error);
            }
        });
    }

    private void limpiarFiltros() {
        buscarField.setText("");
        if (categoriaCombo.getSelectedIndex() == 0) {
            actualizarNoticias();
        } else {
            categoriaCombo.setSelectedIndex(0);
        }
    }

    private void verNoticia() {
        Noticia seleccionada = noticiaSeleccionada();
        if (seleccionada == null) {
            avisarSeleccion();
            return;
        }
        ejecutar(() -> conexion.getServicio().obtenerNoticia(seleccionada.id()),
                noticia -> DetalleNoticiaDialog.mostrar(this, noticia), this::manejarFallo);
    }

    private void iniciarSesion() {
        LoginDialog.Credenciales credenciales = LoginDialog.solicitar(this);
        if (credenciales == null) {
            return;
        }
        char[] contrasena = credenciales.contrasena();
        ejecutar(() -> {
            try {
                return conexion.getServicio().iniciarSesion(
                        credenciales.usuario(), contrasena);
            } finally {
                Arrays.fill(contrasena, '\0');
            }
        }, nuevaSesion -> {
            sesion = nuevaSesion;
            actualizarEstadoSesion();
            estadoLabel.setText("Sesión iniciada correctamente.");
        }, error -> {
            Arrays.fill(contrasena, '\0');
            manejarFalloLogin(error);
        });
    }

    private void publicarNoticia() {
        if (!haySesion()) {
            informarSesionNecesaria();
            return;
        }
        NoticiaDialog.DatosNoticia datos = NoticiaDialog.solicitar(this, null);
        if (datos == null) {
            return;
        }
        NuevaNoticia nueva = new NuevaNoticia(
                datos.titulo(), datos.contenido(), datos.categoria());
        ejecutar(() -> conexion.getServicio().publicarNoticia(sesion.token(), nueva),
                noticia -> {
                    JOptionPane.showMessageDialog(this, "La noticia fue publicada.");
                    actualizarNoticias();
                }, this::manejarFallo);
    }

    private void editarNoticia() {
        Noticia seleccionada = noticiaSeleccionada();
        if (!esPropia(seleccionada)) {
            JOptionPane.showMessageDialog(this,
                    "Solo puede editar sus propias noticias.", "Acción no permitida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        ejecutar(() -> conexion.getServicio().obtenerNoticia(seleccionada.id()),
                actual -> abrirEdicion(actual), this::manejarFallo);
    }

    private void abrirEdicion(Noticia actual) {
        if (!esPropia(actual)) {
            actualizarAcciones();
            return;
        }
        NoticiaDialog.DatosNoticia datos = NoticiaDialog.solicitar(this, actual);
        if (datos == null) {
            return;
        }
        ActualizacionNoticia cambio = new ActualizacionNoticia(
                datos.titulo(), datos.contenido(), datos.categoria());
        ejecutar(() -> conexion.getServicio().editarNoticia(
                        sesion.token(), actual.id(), cambio, actual.version()),
                noticia -> {
                    JOptionPane.showMessageDialog(this, "La noticia fue actualizada.");
                    actualizarNoticias();
                }, error -> {
                    if (error instanceof ConflictoEdicionException) {
                        JOptionPane.showMessageDialog(this,
                                "La noticia fue modificada desde que se abrió.\n"
                                        + "Actualice la información antes de volver a editarla.",
                                "Conflicto de edición", JOptionPane.WARNING_MESSAGE);
                        actualizarNoticias();
                    } else {
                        manejarFallo(error);
                    }
                });
    }

    private void eliminarNoticia() {
        Noticia seleccionada = noticiaSeleccionada();
        if (!esPropia(seleccionada)) {
            JOptionPane.showMessageDialog(this,
                    "Solo puede eliminar sus propias noticias.", "Acción no permitida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar la noticia seleccionada?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        ejecutar(() -> {
            conexion.getServicio().eliminarNoticia(sesion.token(), seleccionada.id());
            return null;
        }, ignorado -> {
            JOptionPane.showMessageDialog(this, "La noticia fue eliminada.");
            actualizarNoticias();
        }, this::manejarFallo);
    }

    private void cerrarSesion() {
        if (!haySesion()) {
            return;
        }
        String token = sesion.token();
        ejecutar(() -> {
            conexion.getServicio().cerrarSesion(token);
            return null;
        }, ignorado -> limpiarSesion("Sesión cerrada."), error -> {
            limpiarSesion("La sesión local fue cerrada.");
            manejarFallo(error);
        });
    }

    private void mostrarReconexion() {
        new ConexionDialog(this, conexion, nuevaConexion -> {
            conexion = nuevaConexion;
            limpiarSesion("Reconectado. Use sus credenciales para volver a iniciar sesión.");
            actualizarNoticias();
        }).setVisible(true);
    }

    private void actualizarEstadoSesion() {
        boolean autenticado = haySesion();
        usuarioLabel.setText(autenticado
                ? "Redactor: " + sesion.autorNombre()
                : "Modo lector");
        loginButton.setVisible(!autenticado);
        accionesRedactor.setVisible(autenticado);
        publicarButton.setEnabled(autenticado);
        logoutButton.setEnabled(autenticado);
        actualizarAcciones();
        revalidate();
        repaint();
    }

    private void actualizarAcciones() {
        Noticia seleccionada = noticiaSeleccionada();
        verButton.setEnabled(seleccionada != null);
        boolean propia = esPropia(seleccionada);
        editarButton.setEnabled(propia);
        eliminarButton.setEnabled(propia);
    }

    private Noticia noticiaSeleccionada() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return null;
        }
        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        return tableModel.getNoticia(filaModelo);
    }

    private boolean esPropia(Noticia noticia) {
        return haySesion() && noticia != null && noticia.autorId() == sesion.autorId();
    }

    private boolean haySesion() {
        return sesion != null && sesion.token() != null;
    }

    private Categoria categoriaSeleccionada() {
        Object seleccion = categoriaCombo.getSelectedItem();
        return seleccion instanceof Categoria categoria ? categoria : null;
    }

    private String nombreCategoria(Object valor) {
        if (!(valor instanceof Categoria categoria)) {
            return String.valueOf(valor);
        }
        return categoria.getNombreVisible();
    }

    private void avisarSeleccion() {
        JOptionPane.showMessageDialog(this, "Seleccione una noticia.",
                "Sin selección", JOptionPane.INFORMATION_MESSAGE);
    }

    private void informarSesionNecesaria() {
        JOptionPane.showMessageDialog(this, "Debe iniciar sesión como redactor.",
                "Sesión requerida", JOptionPane.WARNING_MESSAGE);
    }

    private void limpiarSesion(String mensaje) {
        sesion = null;
        actualizarEstadoSesion();
        estadoLabel.setText(mensaje);
    }

    private void manejarFallo(Throwable error) {
        if (error instanceof AutenticacionException) {
            limpiarSesion("La sesión no es válida o expiró.");
            JOptionPane.showMessageDialog(this,
                    "La sesión no es válida o expiró. Inicie sesión nuevamente.",
                    "Sesión expirada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (error instanceof RemoteException) {
            estadoLabel.setText("Desconectado del servidor. Use Reconectar.");
            JOptionPane.showMessageDialog(this,
                    "Se perdió la comunicación con el servidor. Puede reintentar la conexión.",
                    "Servidor no disponible", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String mensaje = error.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            mensaje = "La operación no pudo completarse.";
        }
        estadoLabel.setText("La operación no pudo completarse.");
        JOptionPane.showMessageDialog(this, mensaje, "Operación no completada",
                JOptionPane.WARNING_MESSAGE);
    }

    private void manejarFalloLogin(Throwable error) {
        if (error instanceof AutenticacionException) {
            estadoLabel.setText("No fue posible iniciar sesión.");
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos, o el usuario está inactivo.",
                    "Autenticación fallida", JOptionPane.WARNING_MESSAGE);
        } else {
            manejarFallo(error);
        }
    }

    private <T> void ejecutar(Callable<T> operacion, Consumer<T> alCompletar,
                              Consumer<Throwable> alFallar) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    alCompletar.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    alFallar.accept(ex);
                } catch (ExecutionException ex) {
                    alFallar.accept(ex.getCause());
                }
            }
        }.execute();
    }
}
