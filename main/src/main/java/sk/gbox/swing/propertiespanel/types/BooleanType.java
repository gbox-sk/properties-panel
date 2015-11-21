package sk.gbox.swing.propertiespanel.types;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import sk.gbox.swing.propertiespanel.*;

/**
 * Property type for boolean values.
 */
@SuppressWarnings("serial")
public class BooleanType extends SimplePropertyType {

    /**
     * Cell editor for strings.
     */
    private static class CellEditor extends DefaultCellEditor {
	private CellEditor() {
	    super(new JCheckBox());
	    setClickCountToStart(1);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
		boolean isSelected, int row, int column) {
	    return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}
    }

    private static class CellRenderer extends JCheckBox implements TableCellRenderer {
	private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

	private Color unselectedForeground;
	private Color unselectedBackground;

	public CellRenderer() {
	    super();
	    setOpaque(true);
	    setBorder(DEFAULT_NO_FOCUS_BORDER);
	    setName("Table.cellRenderer");
	}

	public void setForeground(Color c) {
	    super.setForeground(c);
	    unselectedForeground = c;
	}

	public void setBackground(Color c) {
	    super.setBackground(c);
	    unselectedBackground = c;
	}

	public void updateUI() {
	    super.updateUI();
	    setForeground(null);
	    setBackground(null);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {

	    Color fg = null;
	    Color bg = null;

	    JTable.DropLocation dropLocation = table.getDropLocation();
	    if (dropLocation != null && !dropLocation.isInsertRow()
		    && !dropLocation.isInsertColumn() && dropLocation.getRow() == row
		    && dropLocation.getColumn() == column) {
		isSelected = true;
	    }

	    if (isSelected) {
		super.setForeground(fg == null ? table.getSelectionForeground() : fg);
		super.setBackground(bg == null ? table.getSelectionBackground() : bg);
	    } else {
		Color background = unselectedBackground != null ? unselectedBackground : table
			.getBackground();
		super.setForeground(unselectedForeground != null ? unselectedForeground : table
			.getForeground());
		super.setBackground(background);
	    }

	    setFont(table.getFont());
	    if (!hasFocus) {
		setBorder(DEFAULT_NO_FOCUS_BORDER);
	    }

	    setValue(value);
	    return this;
	}

	/*
	 * The following methods are overridden as a performance measure to to
	 * prune code-paths are often called in the case of renders but which we
	 * know are unnecessary. Great care should be taken when writing your
	 * own renderer to weigh the benefits and drawbacks of overriding
	 * methods like these.
	 */

	public boolean isOpaque() {
	    Color back = getBackground();
	    Component p = getParent();
	    if (p != null) {
		p = p.getParent();
	    }

	    // p should now be the JTable.
	    boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground())
		    && p.isOpaque();
	    return !colorMatch && super.isOpaque();
	}

	public void invalidate() {
	}

	public void validate() {
	}

	public void revalidate() {
	}

	public void repaint(long tm, int x, int y, int width, int height) {
	}

	public void repaint(Rectangle r) {
	}

	public void repaint() {
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	    // Strings get interned...
	    if (propertyName == "selected"
		    || propertyName == "labelFor"
		    || propertyName == "displayedMnemonic"
		    || ((propertyName == "font" || propertyName == "foreground")
			    && oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

		super.firePropertyChange(propertyName, oldValue, newValue);
	    }
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
	}

	protected void setValue(Object value) {
	    boolean booleanValue = false;
	    if (value != null) {
		try {
		    booleanValue = (Boolean) value;
		} catch (Exception ignore) {

		}
	    }
	    setSelected(booleanValue);
	}
    }

    /**
     * Renderer for boolean values.
     */
    private static final CellRenderer renderer = new CellRenderer();

    /**
     * Editor for boolean values.
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
    public Object getDefaultValue() {
	return false;
    }

    @Override
    public boolean isReadOnly() {
	return false;
    }

    @Override
    public boolean checkValue(Object value) {
	try {
	    return (value instanceof Boolean);
	} catch (Exception e) {
	    return false;
	}
    }
}
