package sk.gbox.swing.propertiespanel;

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
     * Returns default value for property type.
     * 
     * @return the default value for properties of this type.
     */
    public Object getDefaultValue() {
	return null;
    }
}
