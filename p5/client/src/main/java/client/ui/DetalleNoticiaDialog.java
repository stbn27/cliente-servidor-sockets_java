package client.ui;

import common.model.Noticia;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public final class DetalleNoticiaDialog extends JDialog {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DetalleNoticiaDialog(Window owner, Noticia noticia) {
        super(owner, "Noticia", ModalityType.APPLICATION_MODAL);
        construirInterfaz(noticia);
    }

    public static void mostrar(Window owner, Noticia noticia) {
        new DetalleNoticiaDialog(owner, noticia).setVisible(true);
    }

    private void construirInterfaz(Noticia noticia) {
        JLabel titulo = new JLabel(noticia.titulo());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JPanel datos = new JPanel(new GridLayout(0, 1, 3, 3));
        datos.add(new JLabel("Categoría: " + nombreCategoria(noticia)));
        datos.add(new JLabel("Autor: " + noticia.autorNombre()));
        datos.add(new JLabel("Creación: " + formatear(noticia.fechaCreacion())));
        datos.add(new JLabel("Última modificación: "
                + formatear(noticia.fechaModificacion())));

        JPanel cabecera = new JPanel(new BorderLayout(4, 8));
        cabecera.add(titulo, BorderLayout.NORTH);
        cabecera.add(datos, BorderLayout.CENTER);

        JTextArea contenido = new JTextArea(noticia.contenido());
        contenido.setEditable(false);
        contenido.setLineWrap(true);
        contenido.setWrapStyleWord(true);
        contenido.setCaretPosition(0);

        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(event -> dispose());
        JPanel acciones = new JPanel();
        acciones.add(cerrar);

        add(cabecera, BorderLayout.NORTH);
        add(new JScrollPane(contenido), BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(cerrar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(680, 480));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private String formatear(java.time.LocalDateTime fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    private String nombreCategoria(Noticia noticia) {
        return noticia.categoria().getNombreVisible();
    }
}
