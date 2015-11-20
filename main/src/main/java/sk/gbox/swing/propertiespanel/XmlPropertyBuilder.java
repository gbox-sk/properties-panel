package sk.gbox.swing.propertiespanel;

import java.util.*;
import org.w3c.dom.*;

/**
 * Simple builder of properties from an xml configuration.
 */
public class XmlPropertyBuilder {

    @SuppressWarnings("serial")
    public static class InvalidConfigurationException extends RuntimeException {

	public InvalidConfigurationException(String message, Throwable cause) {
	    super(message, cause);
	}

	public InvalidConfigurationException(String message) {
	    this(message, null);
	}
    }

    /**
     * Interface for resolvers of property types.
     */
    public interface PropertyTypeResolver {

	/**
	 * Returns instance of property type identified by given name and
	 * constructed with respect to given configuration parameters.
	 * 
	 * @param name
	 *            the name of property type.
	 * @param parameters
	 *            the parameters of property type.
	 * @return the property type, or null, if the specified property type
	 *         cannot be resolved.
	 */
	public PropertyType resolvePropertyType(String name, Map<String, String> parameters);
    }

    /**
     * Default resolver of property types used in the case when all other
     * methods for resolving property type failed.
     */
    private PropertyTypeResolver defaultPropertyTypeResolver = null;

    /**
     * Creates composed property according to content of an xml document
     * specifying properties.
     * 
     * @param xmlDocument
     *            the parsed xml document.
     * @return the composed property.
     */
    public Property createProperties(Document xmlDocument) {
	Element rootElement = xmlDocument.getDocumentElement();
	if ("property".equals(rootElement.getNodeName())) {
	    return createProperty(rootElement);
	}

	if ("properties".equals(rootElement.getNodeName())) {
	    ComposedProperty result = new ComposedProperty();
	    processSubproperties(rootElement, result);
	    return result;
	}

	throw new InvalidConfigurationException(
		"Only \"properties\" and \"property\" are supported as root elements.");
    }

    /**
     * Creates a property according to an xml configuration.
     * 
     * @param propertyElement
     *            the xml element with property configuration.
     * @return the constructed property.
     */
    public Property createProperty(Element propertyElement) {
	if (!"property".equals(propertyElement.getNodeName())) {
	    throw new InvalidConfigurationException(
		    "Element with name \"property\" expected as a parameter.");
	}

	// Read property name
	String propertyName = propertyElement.getAttribute("name");

	// Check subproperties
	Element subpropertiesElement = getChildElementWithName(propertyElement, "subproperties");

	// Find name of property type.
	String typeName = propertyElement.getAttribute("type");
	Element typeElement = getChildElementWithName(propertyElement, "type");
	if (typeElement != null) {
	    typeName = typeElement.getAttribute("name");
	}

	// Normalize name of type.
	if (typeName != null) {
	    typeName = typeName.trim();
	    if (typeName.isEmpty()) {
		typeName = null;
	    }
	}

	// Check presence of name of property type for simple properties
	// (without subproperties)
	if ((subpropertiesElement == null) && (typeName == null)) {
	    throw new InvalidConfigurationException(
		    "Name of property type is missing for a simple property (name of property: \""
			    + propertyName + "\").");
	}

	// Construct property type
	PropertyType propertyType = null;
	try {
	    if (typeName != null) {
		propertyType = processPropertyType(typeName, typeElement);
	    }
	} catch (Exception e) {
	    throw new InvalidConfigurationException("Invalid property type (name of property: \""
		    + propertyName + "\").", e);
	}

	if ((typeName != null) && (propertyType == null)) {
	    throw new InvalidConfigurationException(
		    "Property type not resolvable (name of property: \"" + propertyName + "\").");
	}

	// Instantiate property
	Property result = null;
	if (subpropertiesElement != null) {
	    if (propertyType == null) {
		result = new ComposedProperty();
	    } else {
		result = new ComposedProperty(propertyType);
	    }
	} else {
	    result = new SimpleProperty(propertyType, propertyType.getDefaultValue());
	}

	// Set property attributes
	result.setName(propertyName);
	result.setImportant("true".equals(propertyElement.getAttribute("important")));
	result.setReadOnly("true".equals(propertyElement.getAttribute("readonly")));

	String label = propertyElement.getAttribute("label");
	Element labelElement = getChildElementWithName(propertyElement, "label");
	if (labelElement != null) {
	    label = labelElement.getTextContent();
	}
	result.setLabel(label);

	String hint = propertyElement.getAttribute("hint");
	String hintTitle = null;
	Element hintElement = getChildElementWithName(propertyElement, "hint");
	if (hintElement != null) {
	    hint = hintElement.getTextContent();
	    hintTitle = hintElement.getAttribute("title");
	}
	result.setHint(hint);
	result.setHintTitle(hintTitle);

	// Process subproperties
	if (subpropertiesElement != null) {
	    processSubproperties(subpropertiesElement, (ComposedProperty) result);
	}

	return result;
    }

    /**
     * Processes xml element containing list of properties and attaches them to
     * given parent property.
     * 
     * @param subpropertiesElement
     *            the xml element containing subproperties.
     * @param parent
     *            the parent property to which subproperties are attached.
     */
    private void processSubproperties(Element subpropertiesElement, ComposedProperty parent) {
	if ((subpropertiesElement == null) || (parent == null)) {
	    return;
	}

	NodeList children = subpropertiesElement.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if ((child instanceof Element) && ("property".equals(child.getNodeName()))) {
		parent.getSubproperties().add(createProperty((Element) child));
	    }
	}
    }

    /**
     * Returns a property type satisfying given configuration.
     * 
     * @param typeName
     *            the name of property type.
     * @param propertyTypeElement
     *            the xml element with configuration of property type.
     * @return the instance of property type or null, if the property type
     *         cannot be created or resolved.
     */
    private PropertyType processPropertyType(String typeName, Element propertyTypeElement) {
	Map<String, String> parameters = new HashMap<String, String>();
	if (propertyTypeElement != null) {
	    NodeList children = propertyTypeElement.getChildNodes();
	    for (int i = 0; i < children.getLength(); i++) {
		Node child = children.item(i);
		if ((child instanceof Element) && "parameter".equals(child.getNodeName())) {
		    Element parameterElement = (Element) child;
		    parameters.put(parameterElement.getAttribute("name"),
			    parameterElement.getAttribute("value"));
		}
	    }
	}

	return resolvePropertyType(typeName, parameters);
    }

    /**
     * Executes resolving of property type.
     * 
     * @param name
     *            the name of property type.
     * @param parameters
     *            the map with parameters of property type.
     * @return
     */
    private PropertyType resolvePropertyType(String name, Map<String, String> parameters) {
	if (defaultPropertyTypeResolver != null) {
	    return defaultPropertyTypeResolver.resolvePropertyType(name, parameters);
	}

	return null;
    }

    /**
     * Returns the first child element with given name.
     * 
     * @param element
     *            the element whose children are scanned.
     * @param name
     *            the name of child element.
     * @return the element or null, if the specified child element is not found.
     */
    private static Element getChildElementWithName(Element element, String name) {
	if (element == null) {
	    return null;
	}

	NodeList children = element.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if ((child instanceof Element) && name.equals(child.getNodeName())) {
		return (Element) child;
	    }
	}

	return null;
    }

    /**
     * Returns the default resolver of property types.
     * 
     * @return the resolver.
     */
    public PropertyTypeResolver getDefaultPropertyTypeResolver() {
	return defaultPropertyTypeResolver;
    }

    /**
     * Sets the default resolver of property types.
     * 
     * @param defaultPropertyTypeResolver
     *            the desired resolver.
     */
    public void setDefaultPropertyTypeResolver(PropertyTypeResolver defaultPropertyTypeResolver) {
	this.defaultPropertyTypeResolver = defaultPropertyTypeResolver;
    }
}
