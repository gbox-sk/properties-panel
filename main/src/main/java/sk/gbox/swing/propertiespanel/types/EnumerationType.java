package sk.gbox.swing.propertiespanel.types;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.table.*;

import sk.gbox.swing.propertiespanel.*;

/**
 * Property type with enumerated values.
 */
@SuppressWarnings("serial")
public class EnumerationType extends SimplePropertyType {

    /**
     * Item of enumeration.
     */
    private static class Item {
	/**
	 * Value of the item.
	 */
	Object value;

	/**
	 * Label of the item.
	 */
	String label;

	@Override
	public String toString() {
	    return label;
	}
    }

    /**
     * Cell editor for enumeration.
     */
    private static class CellEditor extends DefaultCellEditor {

	/**
	 * Search map for associating keys with labels.
	 */
	private Map<Object, Item> searchMap;

	/**
	 * Constructs cell editor.
	 */
	public CellEditor() {
	    super(new JComboBox<Item>());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
		boolean isSelected, int row, int column) {
	    Item item = searchMap.get(value);
	    JComboBox<Item> comboBox = (JComboBox<Item>) super.getTableCellEditorComponent(table,
		    item, isSelected, row, column);
	    return comboBox;
	}

	@Override
	public Object getCellEditorValue() {
	    Object selectedValue = super.getCellEditorValue();
	    if (selectedValue instanceof Item) {
		return ((Item) selectedValue).value;
	    }

	    return null;
	}
    }

    /**
     * Cell renderer for enumerations.
     */
    private static class CellRenderer extends DefaultTableCellRenderer {
	/**
	 * Search map for associating keys with labels.
	 */
	private Map<Object, Item> searchMap;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
	    Item item = searchMap.get(value);
	    return super.getTableCellRendererComponent(table, (item != null) ? item.label : null,
		    isSelected, false, row, column);
	}
    }

    /**
     * Renderer for string values.
     */
    private static final CellRenderer renderer = new CellRenderer();

    /**
     * Editor for string values.
     */
    private static final CellEditor editor = new CellEditor();

    /**
     * Items of the enumeration type.
     */
    private final Item[] items;

    /**
     * Model for combo-box.
     */
    private final DefaultComboBoxModel<Item> model;

    /**
     * Map for fast searching of item indices.
     */
    private final Map<Object, Item> searchMap;

    /**
     * Constructs enumeration type.
     * 
     * @param items
     *            the list of items.
     */
    public EnumerationType(List<Object> items) {
	this(items, null);
    }

    /**
     * Constructs enumeration type.
     * 
     * @param items
     *            the map mapping keys to labels.
     */
    public EnumerationType(Map<Object, String> items) {
	this(new ArrayList<Object>(items.keySet()), items);
    }

    /**
     * Constructs enumeration type.
     * 
     * @param items
     *            the list of items.
     * @param itemLabels
     *            the map mapping labels to items.
     */
    public EnumerationType(List<Object> items, Map<Object, String> itemLabels) {
	if (items.isEmpty()) {
	    throw new RuntimeException("Empty enumeration is not allowed.");
	}

	// Check duplicates
	Set<Object> itemSet = new HashSet<Object>(items);
	if (itemSet.size() != items.size()) {
	    throw new RuntimeException("Duplicates are not allowed in the enumeration.");
	}

	if (itemLabels == null) {
	    itemLabels = Collections.emptyMap();
	}

	// Create item list
	this.items = new Item[items.size()];
	int idx = 0;
	for (Object value : items) {
	    Item item = new Item();
	    item.value = value;
	    item.label = itemLabels.get(value);
	    if (item.label == null) {
		if (value != null) {
		    item.label = value.toString();
		} else {
		    item.label = "";
		}
	    }

	    this.items[idx] = item;
	    idx++;
	}

	// Create model
	model = new DefaultComboBoxModel<Item>(this.items);

	// Create search map
	searchMap = new HashMap<Object, Item>();
	for (int i = 0; i < this.items.length; i++) {
	    searchMap.put(this.items[i].value, this.items[i]);
	}
    }

    @Override
    public TableCellRenderer getValueRenderer(PropertiesPanel propertiesPanel) {
	renderer.searchMap = searchMap;
	return renderer;
    }

    @Override
    public TableCellEditor getValueEditor(PropertiesPanel propertiesPanel) {
	@SuppressWarnings("unchecked")
	JComboBox<Item> comboBox = (JComboBox<Item>) editor.getComponent();
	comboBox.setModel(model);
	editor.searchMap = searchMap;
	return editor;
    }

    @Override
    public boolean isReadOnly() {
	return false;
    }

    @Override
    public Object getDefaultValue() {
	return items[0].value;
    }

    @Override
    public boolean checkValue(Object value) {
	return searchMap.containsKey(value);
    }
}
