package sk.gbox.swing.propertiespanel.types;

import java.awt.Component;
import java.awt.event.*;
import java.text.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

import sk.gbox.swing.propertiespanel.*;

/**
 * Property type for floating point number.
 */
@SuppressWarnings("serial")
public class DecimalType extends SimplePropertyType {

    /**
     * Number formatter.
     */
    private static class FloatingPointNumberFormatter extends DefaultFormatter {
	/**
	 * Indicates whether empty string corresponds to the null value.
	 */
	private final boolean nullable;

	/**
	 * Minimal allowed value.
	 */
	private final double minValue;

	/**
	 * Maximal allowed value.
	 */
	private final double maxValue;

	/**
	 * Constructs a floating point number formatter.
	 * 
	 * @param minValue
	 *            the minimal allowed value.
	 * @param maxValue
	 *            the maximal allowed value.
	 * @param nullable
	 *            true, if empty string is considered as null values.
	 */
	public FloatingPointNumberFormatter(double minValue, double maxValue, boolean nullable) {
	    this.minValue = minValue;
	    this.maxValue = maxValue;
	    this.nullable = nullable;
	    setOverwriteMode(false);
	}

	@Override
	public Object stringToValue(String text) throws ParseException {
	    if (text != null)
		text = text.trim();

	    if ((text == null) || text.isEmpty()) {
		if (nullable) {
		    return null;
		} else {
		    throw new ParseException("Empty/null value is not allowed.", 0);
		}
	    }

	    double value;
	    try {
		value = Double.parseDouble(text);
	    } catch (Exception e) {
		throw new ParseException("Parsing \"" + text
			+ "\" to a floating point numeric value failed.", 0);
	    }

	    if ((value < minValue) || (value > maxValue)) {
		throw new ParseException("Parsing \"" + text
			+ "\" to a floating point number in the range [" + minValue + ", "
			+ maxValue + "] failed.", 0);
	    }

	    return value;
	}

	@Override
	public String valueToString(Object value) throws ParseException {
	    if (value == null)
		return "";
	    else
		return value.toString();
	}
    }

    /**
     * Cell editor for floating point numbers.
     */
    private static class CellEditor extends DefaultCellEditor {

	/**
	 * Currently set formatter factory.
	 */
	private DefaultFormatterFactory formatterFactory;

	/**
	 * Constructs editor.
	 */
	public CellEditor() {
	    super(new JFormattedTextField());
	    final JFormattedTextField ftf = (JFormattedTextField) getComponent();

	    // Set GUI behaviour of text field
	    ftf.setValue(null);
	    ftf.setHorizontalAlignment(JTextField.LEADING);
	    ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

	    // Set that one click on cell is enough for editing
	    setClickCountToStart(1);

	    // Special handling code for ENTER
	    ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
	    ftf.getActionMap().put("check", new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    if (!ftf.isEditValid()) {
			if (askEditOrRevert(ftf, null)) {
			    ftf.setValue(ftf.getValue());
			    ftf.postActionEvent();
			}
		    } else
			try {
			    ftf.commitEdit();
			    ftf.postActionEvent();
			} catch (java.text.ParseException exc) {
			    // nothing to do
			}
		}
	    });
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
		boolean isSelected, int row, int column) {
	    JFormattedTextField ftf = (JFormattedTextField) getComponent();
	    ftf.setFormatterFactory(null);
	    ftf.setValue(value);
	    ftf.setFormatterFactory(formatterFactory);
	    return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	@Override
	public Object getCellEditorValue() {
	    JFormattedTextField ftf = (JFormattedTextField) getComponent();
	    return ftf.getValue();
	}

	@Override
	public boolean stopCellEditing() {
	    JFormattedTextField ftf = (JFormattedTextField) getComponent();
	    if (ftf.isEditValid()) {
		try {
		    ftf.commitEdit();
		} catch (java.text.ParseException exc) {
		    // nothing to do
		}
	    } else {
		if (!askEditOrRevert(ftf, null)) {
		    return false;
		} else {
		    ftf.setValue(ftf.getValue());
		}
	    }
	    return super.stopCellEditing();
	}
    }

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

    /**
     * Formatter factory for formatting valid values.
     */
    private final DefaultFormatterFactory formatterFactory;

    /**
     * Default value.
     */
    private final Object defaultValue;

    /**
     * Indicates whether null is accepted as a value.
     */
    private final boolean nullable;

    /**
     * The minimal allowed value.
     */
    private final double minValue;

    /**
     * The maximal allowed value.
     */
    private final double maxValue;

    /**
     * Constructs new decimal type for floating point numbers in given range
     * limit.
     * 
     * @param minValue
     *            the minimal allowed value.
     * @param maxValue
     *            the maximal allowed value.
     * @param nullable
     *            indicates whether null value (empty string) are allowed.
     */
    public DecimalType(double minValue, double maxValue, boolean nullable) {
	if (!(minValue <= maxValue)) {
	    throw new RuntimeException("minValue is not equal or less than maxValue.");
	}

	formatterFactory = new DefaultFormatterFactory(new FloatingPointNumberFormatter(minValue,
		maxValue, nullable));

	this.nullable = nullable;
	this.minValue = minValue;
	this.maxValue = maxValue;

	if (nullable) {
	    defaultValue = null;
	} else {
	    if ((minValue <= 0) && (0 <= maxValue)) {
		defaultValue = Double.valueOf(0);
	    } else {
		defaultValue = minValue;
	    }
	}
    }

    /**
     * Constructs new decimal type for floating point numbers in given range
     * limit.
     * 
     * @param minValue
     *            the minimal allowed value.
     * @param maxValue
     *            the maximal allowed value.
     */
    public DecimalType(double minValue, double maxValue) {
	this(minValue, maxValue, false);
    }

    /**
     * Constructs new integer type for integer values in range limit for long
     * values.
     */
    public DecimalType() {
	this(-Double.MAX_VALUE, Double.MAX_VALUE);
    }

    @Override
    public TableCellRenderer getValueRenderer(PropertiesPanel propertiesPanel) {
	return renderer;
    }

    @Override
    public TableCellEditor getValueEditor(PropertiesPanel propertiesPanel) {
	editor.formatterFactory = formatterFactory;
	return editor;
    }

    @Override
    public boolean isReadOnly() {
	return false;
    }

    @Override
    public Object getDefaultValue() {
	return defaultValue;
    }

    @Override
    public boolean checkValue(Object value) {
	if (value == null) {
	    return nullable;
	}

	try {
	    double doubleValue = ((Number) value).doubleValue();
	    return (minValue <= doubleValue) && (doubleValue <= maxValue);
	} catch (Exception e) {
	    return false;
	}
    }
}
