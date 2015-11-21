package sk.gbox.swing.propertiespanel;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Composed property that is formed by sub-properties.
 */
final public class ComposedProperty extends Property {

    /**
     * Container of properties of the group.
     */
    public class PropertyList extends AbstractList<Property> {

	/**
	 * Ordered list of properties.
	 */
	private final ArrayList<Property> propertyList = new ArrayList<>();

	/**
	 * Set of properties.
	 */
	private final HashSet<Property> propertySet = new HashSet<>();

	@Override
	public Property get(int index) {
	    return propertyList.get(index);
	}

	@Override
	public int size() {
	    return propertyList.size();
	}

	@Override
	public Property set(int index, Property element) {
	    // Avoid nulls
	    if (element == null) {
		throw new NullPointerException("Null value is not allowed.");
	    }

	    // Check whether there is a real change
	    if (propertyList.get(index) == element) {
		return propertyList.get(index);
	    }

	    // Avoid duplicity
	    if (propertySet.contains(element)) {
		throw new IllegalArgumentException(
			"Each property can occur in the group only once.");
	    }

	    // Detach new property from its containing groups.
	    if (element.parent != null) {
		element.parent.getSubproperties().remove(element);
	    }

	    Property old = propertyList.set(index, element);
	    propertySet.add(element);

	    // Detach the former property from this group
	    if (old != null) {
		propertySet.remove(old);
		old.parent = null;
	    }

	    // Attach new property to this group
	    element.parent = ComposedProperty.this;

	    return old;
	}

	@Override
	public void add(int index, Property element) {
	    // Avoid nulls
	    if (element == null) {
		throw new NullPointerException("Null value is not allowed.");
	    }

	    // Avoid duplicity
	    if (propertySet.contains(element)) {
		throw new IllegalArgumentException(
			"Each property can occur in the group only once.");
	    }

	    // Detach element from its containing group
	    if (element.parent != null) {
		element.parent.getSubproperties().remove(element);
	    }

	    propertyList.add(index, element);
	    propertySet.add(element);

	    // Attach new property to this group
	    element.parent = ComposedProperty.this;
	}

	@Override
	public Property remove(int index) {
	    Property old = propertyList.remove(index);
	    propertySet.remove(old);

	    // Detach property from this group
	    old.parent = null;

	    return old;
	}
    }

    /**
     * List of subproperties.
     */
    private final PropertyList subproperties = new PropertyList();

    /**
     * Property value of composed properties.
     */
    private Object value;

    /**
     * Constructs the composed property.
     * 
     * @param type
     *            the type of property.
     * @param initialValue
     *            the initial value of property
     */
    public ComposedProperty(ComposedPropertyType type, Object initialValue) {
	super(type);
	value = initialValue;
    }

    /**
     * Constructs untyped composed property.
     */
    public ComposedProperty(ComposedPropertyType type) {
	this(type, (type != null) ? type.getDefaultValue() : null);
    }

    /**
     * Constructs untyped composed property.
     */
    public ComposedProperty() {
	this(null, null);
    }

    /**
     * Returns list of subproperties that compose this property.
     */
    public PropertyList getSubproperties() {
	return subproperties;
    }

    @Override
    public Object getValue() {
	if (getType() == null) {
	    Map<String, Object> result = new HashMap<String, Object>();
	    retrieveValuesFromSubproperties(result);
	    return result;
	} else {
	    return value;
	}
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setValue(Object value) {
	if (getType() == null) {
	    if ((value == null) || !(value instanceof Map)) {
		throw new RuntimeException("Invalid value.");
	    }
	    pushValuesToSubproperties((Map) value);
	} else {
	    if (this.value == value) {
		return;
	    }

	    if ((value != null) && (value.equals(this.value))) {
		return;
	    }

	    if (!getType().checkValue(value)) {
		throw new RuntimeException("Invalid value.");
	    }

	    this.value = value;
	    pushValuesToSubproperties(((ComposedPropertyType) getType())
		    .splitToSubvalues(this.value));
	    firePropertyValueChanged(this);
	}
    }

    /**
     * Pushes values to subproperties.
     * 
     * @param values
     *            the values of subproperties
     */
    private void pushValuesToSubproperties(@SuppressWarnings("rawtypes") Map values) {
	for (Property property : subproperties) {
	    if (property instanceof SimpleProperty) {
		if (values.containsKey(property.getName())) {
		    property.setValue(values);
		}
	    } else if (property instanceof ComposedProperty) {
		ComposedProperty cProperty = (ComposedProperty) property;
		if (cProperty.getType() == null) {
		    cProperty.pushValuesToSubproperties(values);
		} else {
		    if (values.containsKey(cProperty.getName())) {
			cProperty.setValue(values);
		    }
		}
	    }
	}
    }

    /**
     * Retrieves values from subproperties.
     * 
     * @param output
     *            the map for storing values.
     */
    private void retrieveValuesFromSubproperties(Map<String, Object> output) {
	for (Property property : subproperties) {
	    if (property instanceof SimpleProperty) {
		if (property.getName() != null) {
		    output.put(property.getName(), property.getValue());
		}
	    } else if (property instanceof ComposedProperty) {
		ComposedProperty cProperty = (ComposedProperty) property;
		if (cProperty.getType() == null) {
		    cProperty.retrieveValuesFromSubproperties(output);
		} else {
		    if (cProperty.getName() != null) {
			output.put(cProperty.getName(), cProperty.getValue());
		    }
		}
	    }
	}
    }

    /**
     * Handles change of the list of subproperties of this or descendant
     * composed property.
     * 
     * @param property
     *            the changed property.
     */
    void fireSubpropertyListChanged(ComposedProperty property) {
	if (propertyListeners != null) {
	    for (PropertyListener pl : propertyListeners) {
		pl.subpropertyListChanged(property);
	    }
	}

	if (parent != null) {
	    parent.fireSubpropertyListChanged(property);
	}
    }
}
