package sk.gbox.swing.propertiespanel;

/**
 * An abstract adapter class for receiving property events. The methods in this
 * class are empty. This class exists as convenience for creating listener
 * objects.
 */
public abstract class PropertyAdapter implements PropertyListener {

    @Override
    public void propertyChanged(Property property) {

    }

    @Override
    public void propertyValueChanged(Property property) {

    }

    @Override
    public void subpropertyListChanged(ComposedProperty property) {

    }
}
