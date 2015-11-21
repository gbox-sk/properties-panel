package sk.gbox.swing.propertiespanel;

import java.util.Map;

/**
 * Base class for composed property types. A composed property type is property
 * type that allows to edit a part of its value by subproperties.
 */
public abstract class ComposedPropertyType extends PropertyType {

    /**
     * Split property value to map of sub-values.
     * 
     * @param value
     *            the value.
     * @return map with sub-values (partial values) of given value.
     */
    public abstract Map<String, Object> splitToSubvalues(Object value);

}
