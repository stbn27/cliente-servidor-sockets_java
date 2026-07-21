package client.ui;

import common.model.Categoria;
import common.model.Noticia;
import common.validation.ValidadorNoticia;

import javax.swing.*;
import java.awt.*;

public final class NoticiaDialog extends JDialog {

    private final JTextField tituloField = new JTextField(40);
    private final JComboBox<Categoria> categoriaCombo =
            new JComboBox<>(Categoria.values());
    private final JTextArea contenidoArea = new JTextArea(16, 52);
    private DatosNoticia resultado;

    private NoticiaDialog(Window owner, Noticia noticia) {
        super(owner, noticia == null ? "Publicar noticia" : "Editar noticia",
                ModalityType.APPLICATION_MODAL);
        construirInterfaz();
        if (noticia != null) {
            tituloField.setText(noticia.titulo());
            categoriaCombo.setSelectedItem(noticia.categoria());
            contenidoArea.setText(noticia.contenido());
            contenidoArea.setCaretPosition(0);
        }
    }

    public static DatosNoticia solicitar(Window owner, Noticia noticia) {
        NoticiaDialog dialog = new NoticiaDialog(owner, noticia);
        dialog.setVisible(true);
        return dialog.resultado;
    }

    private void construirInterfaz() {
        contenidoArea.setLineWrap(true);
        contenidoArea.setWrapStyleWord(true);

        JPanel campos = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        campos.add(new JLabel("Título:"), c);
        c.gridx = 1;
        c.weightx = 1;
        campos.add(tituloField, c);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        campos.add(new JLabel("Categoría:"), c);
        c.gridx = 1;
        campos.add(categoriaCombo, c);
        c.gridx = 0;
        c.gridy = 2;
        campos.add(new JLabel("Contenido:"), c);
        c.gridx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        campos.add(new JScrollPane(contenidoArea), c);

        JButton guardar = new JButton("Guardar");
        JButton cancelar = new JButton("Cancelar");
        JPanel acciones = new JPanel();
        acciones.add(guardar);
        acciones.add(cancelar);
        guardar.addActionListener(event -> guardar());
        cancelar.addActionListener(event -> dispose());

        add(campos, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(guardar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(650, 460));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void guardar() {
        String titulo = tituloField.getText().trim();
        String contenido = contenidoArea.getText().trim();
        Categoria categoria = (Categoria) categoriaCombo.getSelectedItem();
        String error = validar(titulo, contenido, categoria);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        resultado = new DatosNoticia(titulo, contenido, categoria);
        dispose();
    }

    private String validar(String titulo, String contenido, Categoria categoria) {
        if (titulo.isEmpty()) {
            return "El título es obligatorio.";
        }
        if (titulo.length() > ValidadorNoticia.TITULO_MAXIMO) {
            return "El título no puede superar " + ValidadorNoticia.TITULO_MAXIMO
                    + " caracteres.";
        }
        if (contenido.isEmpty()) {
            return "El contenido es obligatorio.";
        }
        if (contenido.length() > ValidadorNoticia.CONTENIDO_MAXIMO) {
            return "El contenido no puede superar " + ValidadorNoticia.CONTENIDO_MAXIMO
                    + " caracteres.";
        }
        if (categoria == null) {
            return "La categoría es obligatoria.";
        }
        return null;
    }

    public record DatosNoticia(String titulo, String contenido, Categoria categoria) {
    }
}
