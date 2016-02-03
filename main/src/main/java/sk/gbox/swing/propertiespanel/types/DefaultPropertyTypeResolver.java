package sk.gbox.swing.propertiespanel.types;

import java.util.Map;

import sk.gbox.swing.propertiespanel.PropertyType;
import sk.gbox.swing.propertiespanel.XmlPropertyBuilder.PropertyTypeResolver;

/**
 * Property type resolver for "build-in" property types.
 */
public class DefaultPropertyTypeResolver implements PropertyTypeResolver {

    @SuppressWarnings("unchecked")
    @Override
    public PropertyType resolvePropertyType(String name, Map<String, Object> parameters) {
	switch (name) {
	case "String":
	    return new StringType();
	case "Boolean":
	    return new BooleanType();
	case "Integer":
	    return new IntegerType(readLong(parameters.get("minValue"), Long.MIN_VALUE), readLong(
		    parameters.get("maxValue"), Long.MAX_VALUE), readBoolean(
		    parameters.get("nullable"), false));
	case "Decimal":
	    return new DecimalType(readDouble(parameters.get("minValue"), -Double.MAX_VALUE),
		    readDouble(parameters.get("maxValue"), Double.MAX_VALUE), readBoolean(
			    parameters.get("nullable"), false));
	case "Enumeration":
	    if (parameters.containsKey("items") && (parameters.get("items") instanceof Map)) {
		return new EnumerationType<Object>((Map<Object, String>) parameters.get("items"));
	    } else {
		return null;
	    }
	}

	return null;
    }

    /**
     * Reads parameter value as a long value.
     * 
     * @param parameter
     *            the parameter.
     * @param defaultValue
     *            the default value.
     * @return the value.
     */
    protected long readLong(Object parameter, long defaultValue) {
	try {
	    return Long.parseLong(parameter.toString());
	} catch (Exception e) {
	    return defaultValue;
	}
    }

    /**
     * Reads parameter value as a double value.
     * 
     * @param parameter
     *            the parameter.
     * @param defaultValue
     *            the default value.
     * @return the value.
     */
    protected double readDouble(Object parameter, double defaultValue) {
	try {
	    return Double.parseDouble(parameter.toString());
	} catch (Exception e) {
	    return defaultValue;
	}
    }

    /**
     * Reads parameter value as a boolean value.
     * 
     * @param parameter
     *            the parameter.
     * @param defaultValue
     *            the default value.
     * @return the value.
     */
    protected boolean readBoolean(Object parameter, boolean defaultValue) {
	try {
	    switch (parameter.toString()) {
	    case "true":
		return true;
	    case "false":
		return false;
	    default:
		return defaultValue;
	    }
	} catch (Exception e) {
	    return defaultValue;
	}
    }
}
