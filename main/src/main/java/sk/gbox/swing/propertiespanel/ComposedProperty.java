package sk.gbox.swing.propertiespanel;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;

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
     * Constructs the composed property.
     * 
     * @param type
     *            the type of property
     */
    public ComposedProperty(PropertyType type) {
	super(type);
    }

    /**
     * Constructs untyped composed property.
     */
    public ComposedProperty() {
	this(null);
    }

    /**
     * Returns list of subproperties that compose this property.
     */
    public PropertyList getSubproperties() {
	return subproperties;
    }

    @Override
    public Object getValue() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setValue(Object value) {
	// TODO Auto-generated method stub
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
