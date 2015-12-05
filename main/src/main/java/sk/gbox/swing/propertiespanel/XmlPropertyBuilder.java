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
	public PropertyType resolvePropertyType(String name, Map<String, Object> parameters);
    }

    /**
     * Default resolver of property types used in the case when all other
     * methods for resolving property type failed.
     */
    private PropertyTypeResolver defaultPropertyTypeResolver = null;

    /**
     * Creates composed or simple property according to content of an xml
     * document specifying a property.
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

	    if (rootElement.hasAttribute("hint")) {
		result.setHint(rootElement.getAttribute("hint"));
	    }

	    if (rootElement.hasAttribute("label")) {
		result.setLabel(rootElement.getAttribute("label"));
	    }

	    return result;
	}

	throw new InvalidConfigurationException(
		"Only \"properties\" and \"property\" are supported as root elements.");
    }

    /**
     * Creates composed property - a container for properties defined as
     * children of given xml element. The method does not check name of the
     * referenced container element.
     * 
     * @param xmlProperties
     *            the xml element containing child elements specifying
     *            properties.
     * @return the composed property.
     */
    public ComposedProperty createProperties(Element xmlProperties) {
	if (xmlProperties == null) {
	    throw new NullPointerException("Container elemenent is null.");
	}

	ComposedProperty result = new ComposedProperty();
	processSubproperties(xmlProperties, result);

	if (xmlProperties.hasAttribute("hint")) {
	    result.setHint(xmlProperties.getAttribute("hint"));
	}

	if (xmlProperties.hasAttribute("label")) {
	    result.setLabel(xmlProperties.getAttribute("label"));
	}

	return result;
    }

    /**
     * Creates a property according to an xml configuration. The name of element
     * must be "property".
     * 
     * @param propertyElement
     *            the xml element with property configuration.
     * @return the constructed property.
     */
    public Property createProperty(Element propertyElement) {
	if (!"property".equals(propertyElement.getNodeName())) {
	    throw new InvalidConfigurationException(
		    "An element with name \"property\" expected as a parameter.");
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
		    "Name of property type is missing for a simple property \"" + propertyName
			    + "\".");
	}

	// Construct property type
	PropertyType propertyType = null;
	try {
	    if (typeName != null) {
		propertyType = processPropertyType(typeName, typeElement);
	    }
	} catch (Exception e) {
	    throw new InvalidConfigurationException("Property type \"" + typeName
		    + "\" of property \"" + propertyName + "\" could not be resolved.", e);
	}

	if ((typeName != null) && (propertyType == null)) {
	    throw new InvalidConfigurationException("Property type \"" + typeName
		    + "\" of property \"" + propertyName + "\" could not be resolved.");
	}

	// Instantiate property
	Property result = null;
	if (subpropertiesElement != null) {
	    if (propertyType == null) {
		result = new ComposedProperty();
	    } else {
		if (!(propertyType instanceof ComposedPropertyType)) {
		    throw new InvalidConfigurationException("Property type \"" + typeName
			    + "\" of composed property \"" + propertyName + "\" is not composed.");
		}
		result = new ComposedProperty((ComposedPropertyType) propertyType);
	    }
	} else {
	    if (!(propertyType instanceof SimplePropertyType)) {
		throw new InvalidConfigurationException("Property type \"" + typeName
			+ "\" of composed property \"" + propertyName + "\" is not simple.");
	    }
	    result = new SimpleProperty((SimplePropertyType) propertyType,
		    propertyType.getDefaultValue());
	}

	// Set property attributes
	result.setName(propertyName.isEmpty() ? null : propertyName);
	result.setImportant(readBooleanAttribute(propertyElement, "important", result.isImportant()));
	result.setReadOnly(readBooleanAttribute(propertyElement, "readonly", result.isReadOnly()));

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
	    if (hintElement.hasAttribute("title")) {
		hintTitle = hintElement.getAttribute("title");
	    }
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
	Map<String, Object> parameters = new HashMap<String, Object>();
	if (propertyTypeElement != null) {
	    NodeList children = propertyTypeElement.getChildNodes();
	    for (int i = 0; i < children.getLength(); i++) {
		Node child = children.item(i);
		if (child instanceof Element) {
		    Element childElement = (Element) child;
		    if ("parameter".equals(childElement.getNodeName())) {
			parameters.put(childElement.getAttribute("name"),
				childElement.getAttribute("value"));
		    } else if ("map".equals(childElement.getNodeName())) {
			parameters.put(childElement.getAttribute("name"), readMap(childElement));
		    } else if ("list".equals(childElement.getNodeName())) {
			parameters.put(childElement.getAttribute("name"), readList(childElement));
		    }
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
    private PropertyType resolvePropertyType(String name, Map<String, Object> parameters) {
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
    private Element getChildElementWithName(Element element, String name) {
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
     * Reads element containing items of a map.
     * 
     * @param element
     *            the element containing map items.
     * @return the map with items in the document order.
     */
    private Map<String, String> readMap(Element element) {
	Map<String, String> result = new LinkedHashMap<String, String>();
	NodeList children = element.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if ((child instanceof Element) && ("item".equals(child.getNodeName()))) {
		Element itemElement = (Element) child;
		result.put(itemElement.getAttribute("key"), itemElement.getTextContent());
	    }
	}

	return result;
    }

    /**
     * Reads element containing items of a list.
     * 
     * @param element
     *            the element containing list items.
     * @return the list.
     */
    private List<String> readList(Element element) {
	List<String> result = new ArrayList<String>();
	NodeList children = element.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
	    Node child = children.item(i);
	    if ((child instanceof Element) && ("item".equals(child.getNodeName()))) {
		Element itemElement = (Element) child;
		result.add(itemElement.getTextContent());
	    }
	}

	return result;
    }

    /**
     * Reads boolean attribute (true/false value).
     * 
     * @param element
     *            the element containing the attribute.
     * @param name
     *            the name of the attribute.
     * @param defaultValue
     *            the default value of the attribute, if it is not present.
     * @return the boolean value.
     */
    private boolean readBooleanAttribute(Element element, String name, boolean defaultValue) {
	if (!element.hasAttribute(name)) {
	    return defaultValue;
	}

	String attrValue = element.getAttribute(name);
	if ("true".equals(attrValue)) {
	    return true;
	}

	if ("false".equals(attrValue)) {
	    return false;
	}

	throw new InvalidConfigurationException(
		"Only \"true\" and \"false\" are allowed for attribute (\"" + name
			+ "\") of element \"" + element.getNodeName() + "\".");
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
