package sk.gbox.swing.propertiespanel.types;

import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import sk.gbox.swing.propertiespanel.*;

/**
 * Property type for string values.
 */
public class StringType extends PropertyType {

    /**
     * Cell editor for strings.
     */
    @SuppressWarnings("serial")
    private static class CellEditor extends DefaultCellEditor {
	private CellEditor() {
	    super(new JTextField());
	    final JTextField tf = (JTextField) getComponent();
	    tf.addFocusListener(new FocusAdapter() {
		public void focusGained(final FocusEvent e) {
		    tf.selectAll();
		}
	    });

	    setClickCountToStart(1);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
		boolean isSelected, int row, int column) {
	    return super.getTableCellEditorComponent(table, value, false, row, column);
	}
    }

    @SuppressWarnings("serial")
    private static class CellRenderer extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {

	    return super
		    .getTableCellRendererComponent(table, value, isSelected, false, row, column);
	}
    }

    /**
     * Renderer for string values.
     */
    private static final CellRenderer renderer = new CellRenderer();

    /**
     * Editor for string values.
     */
    private static final CellEditor editor = new CellEditor();

    @Override
    public TableCellRenderer getValueRenderer(PropertiesPanel propertiesPanel) {
	return renderer;
    }

    @Override
    public TableCellEditor getValueEditor(PropertiesPanel propertiesPanel) {
	return editor;
    }

    @Override
    public boolean isReadOnly() {
	return false;
    }
}
