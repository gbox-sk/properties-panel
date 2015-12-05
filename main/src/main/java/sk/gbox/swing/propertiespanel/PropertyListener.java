package sk.gbox.swing.propertiespanel;

/**
 * PropertyListener defines the interface for an object that listens to changes
 * in a Property.
 */
public interface PropertyListener {

    /**
     * Notifies listener when the property or any of its descendant properties
     * changed. The notification includes all changes (e.g. collapse/expand,
     * change of label, change of hint, etc.) with exception of value change.
     * 
     * @param property
     *            the property that was changed.
     */
    void propertyChanged(Property property);

    /**
     * Notifies listener when the value of property or value of any of its
     * descendant properties changed.
     * 
     * @param property
     *            the property whose value was changed.
     */
    void propertyValueChanged(Property property);

    /**
     * Notifies listener that the list of subproperties of the composed property
     * or any of its descendant composed properties changed.
     * 
     * @param property
     *            the composed property that changed its list of subproperties.
     */
    void subpropertyListChanged(ComposedProperty property);
}
