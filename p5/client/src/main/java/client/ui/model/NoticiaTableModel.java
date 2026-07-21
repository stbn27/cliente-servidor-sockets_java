package client.ui.model;

import common.model.Noticia;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class NoticiaTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = {
            "Título", "Categoría", "Autor", "Creación", "Modificación"
    };
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private List<Noticia> noticias = List.of();

    public void setNoticias(List<Noticia> nuevasNoticias) {
        noticias = nuevasNoticias == null
                ? List.of()
                : List.copyOf(new ArrayList<>(nuevasNoticias));
        fireTableDataChanged();
    }

    public Noticia getNoticia(int filaModelo) {
        return noticias.get(filaModelo);
    }

    @Override
    public int getRowCount() {
        return noticias.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNAS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Noticia noticia = noticias.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> noticia.titulo();
            case 1 -> nombreCategoria(noticia);
            case 2 -> noticia.autorNombre();
            case 3 -> noticia.fechaCreacion() == null ? "" :
                    FORMATO_FECHA.format(noticia.fechaCreacion());
            case 4 -> noticia.fechaModificacion() == null ? "" :
                    FORMATO_FECHA.format(noticia.fechaModificacion());
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    private String nombreCategoria(Noticia noticia) {
        if (noticia.categoria() == null) {
            return "";
        }
        return noticia.categoria().getNombreVisible();
    }
}
