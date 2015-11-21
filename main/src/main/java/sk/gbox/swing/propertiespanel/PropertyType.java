package sk.gbox.swing.propertiespanel;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.*;

/**
 * Base class for property types. Any subclass of the property type is
 * recommended to be immutable.
 */
public abstract class PropertyType {

    /**
     * Returns cell renderer for values of this property type.
     * 
     * @param propertiesPanel
     *            the properties panel for which the renderer is provided.
     * 
     * @return the cell renderer for values of this property type. The method
     *         never returns null.
     */
    public abstract TableCellRenderer getValueRenderer(PropertiesPanel propertiesPanel);

    /**
     * Returns cell editor for editable properties of this property type.
     * 
     * @param propertiesPanel
     *            the properties panel for which the editor is provided.
     * @return the cell editor for values of this property type.
     */
    public abstract TableCellEditor getValueEditor(PropertiesPanel propertiesPanel);

    /**
     * Returns whether values of this property type are read-only.
     * 
     * @return true, if values of this property type are read-only. If the
     *         property type is not read-only, it must provide value editor.
     */
    public abstract boolean isReadOnly();

    /**
     * Returns whether given value is value value for the property type.
     * 
     * @param value
     *            the value to be checked.
     * @return true, if the value is valid for the property type, false
     *         otherwise.
     */
    public abstract boolean checkValue(Object value);

    /**
     * Returns default value for property type.
     * 
     * @return the default value for properties of this type.
     */
    public Object getDefaultValue() {
	return null;
    }

    /**
     * Asks the user whether to continue edit or revert the invalid value.
     * 
     * @param editedComponent
     *            the component where the value is entered.
     * @param message
     *            the additional information why the value is not valid.
     * @return true, if the user prefers to revert the value.
     */
    protected static boolean userSaysRevert(Component editedComponent, String message) {
	String windowMessage = "The value is invalid.\n" + "You can either continue editing "
		+ "or revert to the last valid value.";
	Object[] options = { "Edit", "Revert" };
	int answer = JOptionPane.showOptionDialog(
		(editedComponent != null) ? SwingUtilities.getWindowAncestor(editedComponent)
			: null, windowMessage, "Invalid value entered", JOptionPane.YES_NO_OPTION,
		JOptionPane.ERROR_MESSAGE, null, options, options[1]);

	if (answer == 1) {
	    return true;
	}

	return false;
    }
}
