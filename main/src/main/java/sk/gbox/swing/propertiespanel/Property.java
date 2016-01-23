package sk.gbox.swing.propertiespanel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A property.
 */
public abstract class Property {

    /**
     * Composed property to which the property belongs.
     */
    ComposedProperty parent;

    /**
     * Type of the property.
     */
    final PropertyType type;

    /**
     * Name of the property (internal identifier)
     */
    private String name;

    /**
     * Label (visible name) of the property.
     */
    private String label;

    /**
     * Indicates whether value the property is read-only.
     */
    boolean readOnly;

    /**
     * Returns whether the property is important for the user, i.e., it should
     * be visually distinguished.
     */
    boolean important;

    /**
     * Title of the hint.
     */
    private String hintTitle;

    /**
     * Description of the property.
     */
    private String hint;

    /**
     * Internal list of property listeners.
     */
    List<PropertyListener> propertyListeners;

    /**
     * Constructs new property.
     * 
     * @param type
     *            the type of the property.
     */
    Property(PropertyType type) {
	this.type = type;
	this.readOnly = ((this.type == null) || ((this.type != null) && (this.type
		.getValueEditor(null) == null)));
    }

    public PropertyType getType() {
	return type;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	if (this.label == label) {
	    return;
	}

	if ((label != null) && (label.equals(this.label))) {
	    return;
	}

	this.label = label;
	firePropertyChanged(this);
    }

    /**
     * Returns the composed property to which the property belongs.
     */
    public ComposedProperty getParent() {
	return parent;
    }

    public boolean isReadOnly() {
	return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
	if (this.readOnly == readOnly) {
	    return;
	}

	if (readOnly == false) {
	    if ((this.type == null) || ((this.type != null) && (this.type.isReadOnly()))) {
		throw new RuntimeException("The property cannot be edited.");
	    }
	}

	this.readOnly = readOnly;
	firePropertyChanged(this);
    }

    public boolean isImportant() {
	return important;
    }

    public void setImportant(boolean important) {
	if (this.important == important) {
	    return;
	}

	this.important = important;
	firePropertyChanged(this);
    }

    public String getHintTitle() {
	return hintTitle;
    }

    public void setHintTitle(String hintTitle) {
	if (this.hintTitle == hintTitle) {
	    return;
	}

	if ((hintTitle != null) && hintTitle.equals(this.hintTitle)) {
	    return;
	}

	this.hintTitle = hintTitle;
	firePropertyChanged(this);
    }

    public String getHint() {
	return hint;
    }

    public void setHint(String hint) {
	if (this.hint == hint) {
	    return;
	}

	if ((hint != null) && hint.equals(this.hint)) {
	    return;
	}

	this.hint = hint;
	firePropertyChanged(this);
    }

    /**
     * Returns the current value of the property.
     */
    public abstract Object getValue();

    /**
     * Sets the current value of the property.
     * 
     * @param value
     *            the desired value of the property.
     */
    public abstract void setValue(Object value);

    /**
     * Resets value of the property to default value. If the property is
     * composed property, all subproperties are reset.
     */
    public abstract void resetToDefaultValue();

    /**
     * Adds a listener to the list that is notified each time a change the
     * property or its descendant property occurs.
     * 
     * @param propertyListener
     *            the property listener
     */
    public void addPropertyListener(PropertyListener propertyListener) {
	if (propertyListener == null) {
	    return;
	}

	if (propertyListeners == null) {
	    propertyListeners = new CopyOnWriteArrayList<PropertyListener>();
	}

	propertyListeners.add(propertyListener);
    }

    /**
     * Removes a listener from the list that is notified each time a change the
     * property or its descendant property occurs.
     * 
     * @param propertyListener
     *            the property listener
     */
    public void removePropertyListener(PropertyListener propertyListener) {
	if ((propertyListener == null) || (propertyListeners == null)) {
	    return;
	}

	propertyListeners.remove(propertyListener);
    }

    /**
     * Fires that property was changed. This does not include change of property
     * value.
     * 
     * @param changedProperty
     *            the changed property.
     */
    void firePropertyChanged(Property changedProperty) {
	if (propertyListeners != null) {
	    for (PropertyListener pl : propertyListeners) {
		pl.propertyChanged(changedProperty);
	    }
	}

	if (parent != null) {
	    parent.firePropertyChanged(changedProperty);
	}
    }

    /**
     * Fires that property value was changed.
     * 
     * @param changedProperty
     *            the changed property.
     */
    void firePropertyValueChanged(Property changedProperty) {
	if (propertyListeners != null) {
	    for (PropertyListener pl : propertyListeners) {
		pl.propertyValueChanged(changedProperty);
	    }
	}

	if (parent != null) {
	    parent.firePropertyValueChanged(changedProperty);
	}
    }
}
