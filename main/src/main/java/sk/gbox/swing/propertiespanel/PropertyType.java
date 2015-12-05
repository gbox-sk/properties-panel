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
     * Returns whether given value is assignable value for the property type.
     * 
     * @param value
     *            the value to be checked.
     * @return true, if the value is assignable for the property type, false
     *         otherwise.
     */
    public abstract boolean isAssignableValue(Object value);

    /**
     * Converts assignable value to value that is internally valid for the
     * property type. For instance, string value can be assignable, but
     * internally invalid. The method is not required to check, whether the
     * value is assignable.
     * 
     * @param value
     *            the assignable value.
     * @return the internally valid value.
     */
    public Object convertAssignableToValidValue(Object value) {
	return value;
    }

    /**
     * Returns default value for property type.
     * 
     * @return the default value for properties of this type.
     */
    public Object getDefaultValue() {
	return null;
    }

    // ------------------------------------------------------------------
    // Shared static constants for GUI communication.
    // ------------------------------------------------------------------

    private static String editLabel = "Edit";
    private static String revertLabel = "Revert";
    private static String editOrRevertTitle = "Invalid value entered";
    private static String editOrRevertMessage = "The value is invalid.\n"
	    + "You can either continue editing or revert to the last valid value.";

    /**
     * Configure texts displayed in a window displayed when the user entered an
     * invalid value.
     * 
     * @param edit
     *            the text of edit button.
     * @param revert
     *            the text of revert button.
     * @param windowTitle
     *            the title of window.
     * @param message
     *            the message displayed in the window.
     */
    public static void configureEditOrRevertTexts(String edit, String revert, String windowTitle,
	    String message) {
	editLabel = edit;
	revertLabel = revert;
	editOrRevertTitle = windowTitle;
	editOrRevertMessage = message;
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
    protected static boolean askEditOrRevert(Component editedComponent, String message) {
	String windowMessage = String.format(editOrRevertMessage, (message == null) ? "" : message);
	Object[] options = { editLabel, revertLabel };
	int answer = JOptionPane.showOptionDialog(
		(editedComponent != null) ? SwingUtilities.getWindowAncestor(editedComponent)
			: null, windowMessage, editOrRevertTitle, JOptionPane.YES_NO_OPTION,
		JOptionPane.ERROR_MESSAGE, null, options, options[1]);

	if (answer == 1) {
	    return true;
	}

	return false;
    }
}
