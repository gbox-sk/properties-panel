package sk.gbox.swing.propertiespanel;

/**
 * Simple property.
 */
final public class SimpleProperty extends Property {

    /**
     * Current value of the property.
     */
    private Object value;

    /**
     * Constructs new property.
     * 
     * @param type
     *            the type of the property.
     * 
     * @param initialValue
     *            the initial value of the property.
     */
    public SimpleProperty(SimplePropertyType type, Object initialValue) {
	super(type);
	if (type == null) {
	    throw new NullPointerException("Simple property must have a type.");
	}

	if (!type.isAssignableValue(initialValue)) {
	    throw new RuntimeException("Invalid value.");
	}

	value = type.convertAssignableToValidValue(initialValue);
    }

    @Override
    public Object getValue() {
	return value;
    }

    @Override
    public void setValue(Object value) {
	if (this.value == value) {
	    return;
	}

	if ((value != null) && (value.equals(this.value))) {
	    return;
	}

	if (!getType().isAssignableValue(value)) {
	    throw new RuntimeException("Invalid value.");
	}

	this.value = getType().convertAssignableToValidValue(value);
	firePropertyValueChanged(this);
    }

    @Override
    public void resetToDefaultValue() {
	setValue(getType().getDefaultValue());
    }

}
