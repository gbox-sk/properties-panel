package sk.gbox.swing.propertiespanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

@SuppressWarnings("serial")
public class PropertiesPanel extends JPanel {

    // -----------------------------------------------------------------

    /**
     * Data related to a single row of the table.
     */
    private static class PropertyRow {
	/**
	 * Property in the row
	 */
	Property property;

	/**
	 * Indicates that the row is visible
	 */
	boolean visible;

	/**
	 * Indicates whether the property is collapsed.
	 */
	boolean collapsed;

	/**
	 * Indicates whether the property is composite property with
	 * subproperties.
	 */
	boolean composite;

	/**
	 * Row index.
	 */
	int rowIndex;

	/**
	 * Indentation level
	 */
	byte indentationLevel;

	/**
	 * The number of indentation block that are closed at this row.
	 */
	byte indentationClosings;
    }

    // -----------------------------------------------------------------

    /**
     * Cell renderer for property names.
     */
    private class PropertyNameCellRenderer extends DefaultTableCellRenderer {

	/**
	 * Indicates whether the property name is followed by a value.
	 */
	private boolean hasValuePart = false;

	/**
	 * Data related to property.
	 */
	private PropertyRow propertyRow;

	/**
	 * Color of property value separator line.
	 */
	private Color separatorColor;

	/**
	 * Default cell background.
	 */
	private final Color defaultBackground;

	/**
	 * Default cell foreground.
	 */
	private final Color defaultForeground;

	/**
	 * Constructs the renderer.
	 */
	public PropertyNameCellRenderer() {
	    defaultBackground = getBackground();
	    defaultForeground = getForeground();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {

	    if (value == null) {
		return null;
	    }

	    propertyRow = (PropertyRow) value;
	    if (!propertyRow.visible) {
		return null;
	    }

	    hasValuePart = (propertyRow.property.getType() != null);
	    separatorColor = table.getGridColor();

	    setBackground(defaultBackground);
	    setForeground(defaultForeground);

	    if ((!isSelected) && (!hasValuePart)
		    && (propertyRow.property instanceof ComposedProperty)) {
		if (groupNameBackground != null) {
		    setBackground(groupNameBackground);
		}

		if (groupNameForeground != null) {
		    setForeground(groupNameForeground);
		}
	    }

	    String propertyName = propertyRow.property.getLabel();
	    super.getTableCellRendererComponent(table, propertyName, isSelected, false, row, column);

	    int indentation = indentationWidth * propertyRow.indentationLevel;
	    if (propertyRow.composite) {
		indentation += indentationWidth;
	    } else {
		indentation += indentationLevelShift * indentationWidth;
	    }

	    if (propertyRow.property.isImportant()) {
		setFont(getFont().deriveFont(Font.BOLD));
	    }

	    setBorder(new CompoundBorder(getBorder(), new EmptyBorder(0, indentation + 2, 0,
		    hasValuePart ? 2 : 0)));

	    return this;
	}

	@Override
	public void paint(Graphics g) {
	    super.paint(g);
	    final int width = getWidth();
	    final int height = getHeight();

	    // Draw separator line.
	    if (hasValuePart) {
		g.setColor(separatorColor);
		g.drawLine(width - 1, 0, width - 1, height - 1);
	    }

	    // Draw indentation lines
	    if ((treeLineColor != null) && (indentationLevelShift >= 0)) {
		g.setColor(treeLineColor);
		int centerX = indentationWidth / 2;
		int straightLines = propertyRow.indentationLevel - propertyRow.indentationClosings;
		for (int i = 0; i < straightLines; i++) {
		    g.drawLine(centerX, 0, centerX, height - 1);
		    centerX += indentationWidth;
		}

		for (int i = straightLines; i < propertyRow.indentationLevel; i++) {
		    g.drawLine(centerX, 0, centerX, height / 2);
		    g.drawLine(centerX, height / 2, centerX + indentationWidth / 3, height / 2);
		    centerX += indentationWidth;
		}
	    }

	    // Draw collapse/expand icon for composite properties.
	    if (propertyRow.composite) {
		if ((treeLineColor != null) && (indentationLevelShift >= 0)
			&& (!propertyRow.collapsed)) {
		    g.setColor(treeLineColor);
		    int centerX = indentationWidth / 2 + propertyRow.indentationLevel
			    * indentationWidth;
		    g.drawLine(centerX, height / 2, centerX, height - 1);
		}

		BufferedImage icon = (propertyRow.collapsed) ? expandIcon : collapseIcon;
		int iconXShift = propertyRow.indentationLevel * indentationWidth
			+ (indentationWidth - icon.getWidth()) / 2;
		g.drawImage(icon, iconXShift, (height - icon.getHeight()) / 2, null);
	    }
	}
    }

    // -----------------------------------------------------------------

    /**
     * Cell renderer for empty (untyped) properties.
     */
    private static class EmptyCellRenderer extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
	    return super
		    .getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
	}
    }

    // -----------------------------------------------------------------

    /**
     * JTable with custom cell renderers and editors.
     */
    class PropertiesTable extends JTable {

	/**
	 * Left padding added to the value cells.
	 */
	private static final int VALUE_CELL_LEFT_PADDING = 5;

	/**
	 * Constructs the properties table.
	 */
	public PropertiesTable() {
	    // Install mouse handlers for handling collapse/expand actions
	    addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		    if ((e.getButton() != MouseEvent.BUTTON1) || (e.getClickCount() < 2)) {
			return;
		    }

		    int row = rowAtPoint(e.getPoint());
		    int col = columnAtPoint(e.getPoint());
		    if ((row < 0) || (col < 0)) {
			return;
		    }

		    if ((col != 0) && !isFullySpannedRow(row)) {
			return;
		    }

		    PropertyRow propertyRow = getPropertyRow(row);
		    if (!propertyRow.composite) {
			return;
		    }

		    Rectangle cellRect = getCellRect(row, col, false);
		    if (e.getX() - cellRect.x > (propertyRow.indentationLevel + 1)
			    * indentationWidth) {
			changeCollapseOfProperty(propertyRow);
		    }
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		    if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		    }

		    int row = rowAtPoint(e.getPoint());
		    int col = columnAtPoint(e.getPoint());
		    if ((row < 0) || (col < 0)) {
			return;
		    }

		    if ((col != 0) && !isFullySpannedRow(row)) {
			return;
		    }

		    PropertyRow propertyRow = getPropertyRow(row);
		    if (!propertyRow.composite) {
			return;
		    }

		    Rectangle cellRect = getCellRect(row, col, false);
		    int distanceFromLeft = e.getX() - cellRect.x;
		    int iconLeftPos = propertyRow.indentationLevel * indentationWidth;
		    if ((iconLeftPos <= distanceFromLeft)
			    && (distanceFromLeft <= iconLeftPos + indentationWidth)) {
			changeCollapseOfProperty(propertyRow);
		    }
		}
	    });

	    // Set selection listener for displaying hints.
	    getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
		    if (e.getValueIsAdjusting()) {
			return;
		    }

		    updateHintBox();
		}
	    });
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
	    if (column == 0) {
		return propertyNameCellRenderer;
	    }

	    PropertyRow propertyRow = getPropertyRow(row);
	    PropertyType propertyType = propertyRow.property.getType();

	    TableCellRenderer result = null;
	    if (propertyType != null) {
		result = propertyType.getValueRenderer(PropertiesPanel.this);
	    }

	    return (result != null) ? result : emptyCellRenderer;
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
	    Component result = super.prepareRenderer(renderer, row, column);
	    if (result == null) {
		return null;
	    }

	    if (result != null) {
		result.setEnabled(true);
	    }

	    if ((!(renderer instanceof PropertyNameCellRenderer)) && (result instanceof JComponent)) {
		JComponent component = (JComponent) result;
		component.setBorder(BorderFactory.createEmptyBorder(0, VALUE_CELL_LEFT_PADDING, 0,
			0));

		PropertyRow propertyRow = getPropertyRow(row);
		if (propertyRow.property.isReadOnly()) {
		    component.setEnabled(false);
		}
	    }

	    return result;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
	    if (column == 0) {
		return null;
	    }

	    PropertyRow propertyRow = getPropertyRow(row);
	    PropertyType propertyType = propertyRow.property.getType();
	    if (propertyType == null) {
		return null;
	    }

	    return propertyType.getValueEditor(PropertiesPanel.this);
	}

	@Override
	public Component prepareEditor(TableCellEditor editor, int row, int column) {
	    Component result = super.prepareEditor(editor, row, column);
	    if (result == null) {
		return null;
	    }

	    if (result instanceof JComponent) {
		JComponent component = (JComponent) result;
		component.setBorder(BorderFactory.createEmptyBorder(0, VALUE_CELL_LEFT_PADDING, 0,
			0));
	    }

	    return result;
	}

	@Override
	public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
	    if (isFullySpannedRow(row)) {
		Rectangle nameCell = super.getCellRect(row, 0, includeSpacing);
		Rectangle valueCell = super.getCellRect(row, 1, includeSpacing);
		return nameCell.union(valueCell);
	    } else {
		return super.getCellRect(row, column, includeSpacing);
	    }
	}

	/**
	 * Return whether the first cell is spanned in the whole row.
	 * 
	 * @param row
	 *            the index of row
	 * @return true, if the first cell of row is spanned in the whole row.
	 */
	public boolean isFullySpannedRow(int row) {
	    return (getPropertyRow(row).property.getType() == null);
	}

	/**
	 * Returns property row displayed in given table row.
	 */
	private PropertyRow getPropertyRow(int row) {
	    row = convertRowIndexToModel(row);
	    return propertiesTableModel.propertyRows.get(row);
	}

	/**
	 * Executes collapse/expand change of given row.
	 * 
	 * @param propertyRow
	 *            records of changing row/property.
	 */
	private void changeCollapseOfProperty(PropertyRow propertyRow) {
	    if (!propertyRow.composite) {
		return;
	    }

	    int selectedRow = getSelectedRow();
	    propertyRow.collapsed = !propertyRow.collapsed;

	    String propertyName = propertyRow.property.getName();
	    if (propertyName != null) {
		if (propertyRow.collapsed) {
		    collapsedProperties.add(propertyName);
		} else {
		    collapsedProperties.remove(propertyName);
		}
	    }

	    propertiesTableModel.rebuildPropertyRows();

	    if (selectedRow >= 0) {
		setRowSelectionInterval(selectedRow, selectedRow);
	    }
	}

	/**
	 * Updates hint box.
	 */
	private void updateHintBox() {
	    int selectedRow = getSelectedRow();
	    if (selectedRow < 0) {
		if (model != null) {
		    hintTitleLabel.setText(model.getLabel());
		    String hint = model.getHint();
		    if (hint != null) {
			hintLabel.setText("<html>" + model.getHint() + "</html>");
		    } else {
			hintLabel.setText("");
		    }
		} else {
		    hintTitleLabel.setText("");
		    hintLabel.setText("");
		}
		return;
	    }

	    Property property = getPropertyRow(selectedRow).property;
	    String hintTitle = property.getHintTitle();
	    if (hintTitle == null) {
		hintTitle = property.getLabel();
	    }

	    hintTitleLabel.setText(hintTitle);

	    String hint = property.getHint();
	    if (hint != null) {
		hintLabel.setText("<html>" + property.getHint() + "</html>");
	    } else {
		hintLabel.setText("");
	    }
	}
    }

    // -----------------------------------------------------------------

    /**
     * Table model for PropertiesTable
     *
     */
    private class PropertiesTableModel extends AbstractTableModel implements PropertyListener {

	/**
	 * Mapping of properties to associated property rows.
	 */
	private final HashMap<Property, PropertyRow> propertyMap = new HashMap<Property, PropertyRow>();

	/**
	 * Ordered list of visible property rows.
	 */
	private final ArrayList<PropertyRow> propertyRows = new ArrayList<PropertyRow>();

	@Override
	public int getColumnCount() {
	    return 2;
	}

	@Override
	public int getRowCount() {
	    return propertyRows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
	    PropertyRow row = propertyRows.get(rowIndex);
	    if (columnIndex == 0) {
		return row;
	    }

	    PropertyType propertyType = row.property.getType();
	    if (propertyType == null) {
		return null;
	    }

	    return row.property.getValue();
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	    if (columnIndex != 1) {
		return;
	    }

	    Property property = propertyRows.get(rowIndex).property;
	    if (property.readOnly) {
		return;
	    }

	    property.setValue(aValue);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
	    Property property = propertyRows.get(rowIndex).property;
	    return !property.isReadOnly();
	}

	@Override
	public void propertyChanged(Property property) {
	    PropertyRow row = propertyMap.get(property);
	    if (row == null) {
		return;
	    }

	    fireTableRowsUpdated(row.rowIndex, row.rowIndex);
	}

	@Override
	public void propertyValueChanged(Property property) {
	    propertyChanged(property);
	}

	@Override
	public void subpropertyListChanged(ComposedProperty property) {
	    rebuildPropertyRows();
	}

	/**
	 * Rebuilds property rows.
	 */
	private void rebuildPropertyRows() {
	    // Mark all currenty visible rows as invisible
	    for (PropertyRow row : propertyRows) {
		row.visible = false;
	    }

	    // Clear property rows
	    propertyRows.clear();

	    if (model == null) {
		fireTableDataChanged();
		return;
	    }

	    // Rebuild rows
	    PropertyRow virtualRoot = new PropertyRow();
	    virtualRoot.indentationLevel = -1;
	    rebuildPropertyRows(model, virtualRoot);

	    // Compute indentation closings
	    PropertyRow previous = null;
	    for (PropertyRow propertyRow : propertyRows) {
		if (previous == null) {
		    previous = propertyRow;
		    continue;
		}

		previous.indentationClosings = (byte) Math.max(previous.indentationLevel
			- propertyRow.indentationLevel, 0);
		previous = propertyRow;
	    }

	    if (previous != null) {
		previous.indentationClosings = previous.indentationLevel;
	    }

	    fireTableDataChanged();
	}

	/**
	 * Recursive helper subroutine to rebuild property rows.
	 */
	private void rebuildPropertyRows(ComposedProperty property, PropertyRow parent) {
	    for (Property subproperty : property.getSubproperties()) {
		PropertyRow row = propertyMap.get(subproperty);

		// Create property record if does not exist
		if (row == null) {
		    row = new PropertyRow();
		    row.property = subproperty;
		    propertyMap.put(subproperty, row);

		    if (subproperty instanceof ComposedProperty) {
			row.collapsed = collapsedProperties.contains(subproperty.getName());
			row.composite = (!((ComposedProperty) subproperty).getSubproperties()
				.isEmpty());
		    }
		}

		// Set indentation
		row.indentationLevel = (byte) (parent.indentationLevel + 1);

		// Add row to visible rows
		row.visible = true;
		row.rowIndex = propertyRows.size();
		propertyRows.add(row);

		// Process subproperies in case of expanded composed property
		if ((subproperty instanceof ComposedProperty) && (!row.collapsed)) {
		    rebuildPropertyRows((ComposedProperty) subproperty, row);
		}
	    }
	}
    }

    // -----------------------------------------------------------------
    // Instance variables
    // -----------------------------------------------------------------

    /**
     * Composed property that is displayed in the model.
     */
    private ComposedProperty model;

    /**
     * Scroll pane.
     */
    private final JScrollPane scrollPane;

    /**
     * Table with properties.
     */
    private final PropertiesTable propertiesTable;

    /**
     * Table model for properties table.
     */
    private final PropertiesTableModel propertiesTableModel;

    /**
     * Event handler that enables resize of columns without header.
     */
    private final TableColumnResizer columnResizer;

    /**
     * Cell renderer for property names.
     */
    private final PropertyNameCellRenderer propertyNameCellRenderer;

    /**
     * Cell renderer for property values without type.
     */
    private final EmptyCellRenderer emptyCellRenderer;

    /**
     * Information panel.
     */
    private final JPanel hintBox;

    /**
     * Component for displaying hint title.
     */
    private final JLabel hintTitleLabel;

    /**
     * Component for displaying hint text.
     */
    private final JLabel hintLabel;

    /**
     * Indicates whether box with hints is visible.
     */
    private boolean hintBoxVisible = true;

    /**
     * Indicates whether title of hint is visible.
     */
    private boolean hintTitleVisible = true;

    /**
     * Icon of for expanded composite property that can be collapsed
     */
    private BufferedImage collapseIcon;

    /**
     * Icon for collapsed composite property that can be expanded.
     */
    private BufferedImage expandIcon;

    /**
     * Color for drawing tree lines.
     */
    private Color treeLineColor;

    /**
     * Background color of untyped composed property that are used only as a
     * property group.
     */
    private Color groupNameBackground;

    /**
     * Foreground color of untyped composed property that are used only as a
     * property group.
     */
    private Color groupNameForeground;

    /**
     * Level indentation in pixels (depends on icon widths)
     */
    private int indentationWidth;

    /**
     * Level shift for simple properties
     */
    private byte indentationLevelShift = 0;

    /**
     * Name set of properties that were collapsed.
     */
    private Set<String> collapsedProperties = new HashSet<String>();

    // -----------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------

    /**
     * Create the panel.
     */
    public PropertiesPanel() {
	setLayout(new BorderLayout(0, 0));

	propertyNameCellRenderer = new PropertyNameCellRenderer();
	emptyCellRenderer = new EmptyCellRenderer();

	scrollPane = new JScrollPane();
	add(scrollPane, BorderLayout.CENTER);

	propertiesTable = new PropertiesTable();
	propertiesTableModel = new PropertiesTableModel();
	propertiesTable.setAutoCreateColumnsFromModel(true);
	propertiesTable.setAutoCreateRowSorter(false);
	propertiesTable.setModel(propertiesTableModel);
	propertiesTable.setRowSorter(null);
	propertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	propertiesTable.getTableHeader().setReorderingAllowed(false);
	propertiesTable.setUI(new PropertiesTableUI());
	propertiesTable.setShowHorizontalLines(true);
	propertiesTable.setShowVerticalLines(false);
	propertiesTable.getColumnModel().setColumnMargin(0);
	updateRowHeights();

	scrollPane.setViewportView(propertiesTable);
	scrollPane.setColumnHeaderView(propertiesTable.getTableHeader());

	hintBox = new JPanel();
	hintBox.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
	add(hintBox, BorderLayout.SOUTH);
	hintBox.setLayout(new BorderLayout(0, 0));

	hintTitleLabel = new JLabel("");
	hintTitleLabel.setFont(hintTitleLabel.getFont().deriveFont(
		hintTitleLabel.getFont().getStyle() | Font.BOLD));
	hintTitleLabel.setBorder(new EmptyBorder(3, 3, 3, 3));
	hintBox.add(hintTitleLabel, BorderLayout.NORTH);

	hintLabel = new JLabel("");
	hintLabel.setBorder(new EmptyBorder(3, 5, 3, 5));
	hintBox.add(hintLabel);

	BufferedImage defaultCollapseIcon = null;
	BufferedImage defaultExpandIcon = null;
	try {
	    defaultCollapseIcon = ImageIO.read(PropertiesPanel.class
		    .getResource("collapse-icon.png"));
	    defaultExpandIcon = ImageIO.read(PropertiesPanel.class.getResource("expand-icon.png"));
	} catch (Exception ignore) {

	}

	columnResizer = new TableColumnResizer(propertiesTable);
	setResizableByDividerLine(true);

	propertiesTable.updateHintBox();
	setTreeUI(defaultCollapseIcon, defaultExpandIcon, getGridColor());
	setHeaderLabels("Property", "Value");
	revalidate();
    }

    // -----------------------------------------------------------------
    // Model
    // -----------------------------------------------------------------

    /**
     * Returns the properties model.
     * 
     * @return the properties model.
     */
    public ComposedProperty getModel() {
	return model;
    }

    /**
     * Sets the properties model.
     * 
     * @param model
     *            the desired model.
     */
    public void setModel(ComposedProperty model) {
	if (model == this.model) {
	    return;
	}

	if (propertiesTable.isEditing()) {
	    propertiesTable.getCellEditor().cancelCellEditing();
	}

	if (this.model != null) {
	    this.model.removePropertyListener(propertiesTableModel);
	}

	propertiesTableModel.propertyMap.clear();
	this.model = model;
	propertiesTableModel.rebuildPropertyRows();

	if (this.model != null) {
	    this.model.addPropertyListener(propertiesTableModel);
	}

	propertiesTable.getSelectionModel().clearSelection();
	propertiesTable.updateHintBox();
	revalidate();
	repaint();
    }

    // -----------------------------------------------------------------
    // Setters and getter for configuring the UI
    // -----------------------------------------------------------------

    /**
     * Configures UI for drawing tree of properties.
     * 
     * @param collapseIcon
     *            the icon of expanded composite property.
     * @param expandIcon
     *            the icon of collapsed composite property.
     * @param lineColorOfTree
     *            the color of lines for drawing tree, if the value is null, no
     *            lines are drawn.
     */
    public void setTreeUI(BufferedImage collapseIcon, BufferedImage expandIcon,
	    Color lineColorOfTree) {

	if ((collapseIcon == null) || (expandIcon == null)) {
	    throw new NullPointerException("Both icons must be set.");
	}

	this.collapseIcon = deepCopy(collapseIcon);
	this.expandIcon = deepCopy(expandIcon);
	this.treeLineColor = lineColorOfTree;
	this.indentationWidth = Math.max(this.collapseIcon.getWidth(), this.expandIcon.getWidth());

	updateRowHeights();
	repaint();
    }

    /**
     * Returns color of lines that visualize the tree of properties
     * 
     * @return the color of lines.
     */
    public Color getLineColorOfTree() {
	return treeLineColor;
    }

    /**
     * Sets color of lines that visualize the tree of properties
     * 
     * @param lineColor
     *            the desired color of lines.
     */
    public void setLineColorOfTree(Color lineColor) {
	treeLineColor = lineColor;
	repaint();
    }

    /**
     * Returns indentation level shift for simple simple subproperties.
     */
    public int getIndentationLevelShift() {
	return indentationLevelShift;
    }

    /**
     * Sets indentation level shift for simple subproperites.
     * 
     * @param indentationLevelShift
     *            the shift values, only -1, 0, 1 are allowed.
     */
    public void setIndentationLevelShift(int indentationLevelShift) {
	indentationLevelShift = Math.min(Math.max(indentationLevelShift, -1), 1);
	if (indentationLevelShift == this.indentationLevelShift) {
	    return;
	}

	this.indentationLevelShift = (byte) indentationLevelShift;
	repaint();
    }

    /**
     * Sets the grid color.
     * 
     * @param gridColor
     *            the desired color of the grid lines.
     */
    public void setGridColor(Color gridColor) {
	propertiesTable.setGridColor(gridColor);
    }

    /**
     * Returns the color of grid lines.
     * 
     * @return the color of grid lines.
     */
    public Color getGridColor() {
	return propertiesTable.getGridColor();
    }

    /**
     * Returns whether hint box is visible.
     * 
     * @return true, if the hint box is visible, false otherwise.
     */
    public boolean isHintBoxVisible() {
	return hintBoxVisible;
    }

    /**
     * Sets whether hint box is visible.
     * 
     * @param hintBoxVisible
     *            the desired visibility of the hint box.
     */
    public void setHintBoxVisible(boolean hintBoxVisible) {
	if (this.hintBoxVisible == hintBoxVisible) {
	    return;
	}

	this.hintBoxVisible = hintBoxVisible;
	if (hintBoxVisible) {
	    add(hintBox, BorderLayout.SOUTH);
	} else {
	    remove(hintBox);
	}

	revalidate();
	repaint();
    }

    /**
     * Returns whether hint title is visible in the hint box.
     * 
     * @return true, if the hint title is visible, false otherwise.
     */
    public boolean isHintTitleVisible() {
	return hintTitleVisible;
    }

    /**
     * Sets whether hint title is visible in the hint box.
     * 
     * @param hintTitleVisible
     *            the desired visibility of hint title in the hint box.
     */
    public void setHintTitleVisible(boolean hintTitleVisible) {
	if (this.hintTitleVisible == hintTitleVisible) {
	    return;
	}

	this.hintTitleVisible = hintTitleVisible;
	if (hintTitleVisible) {
	    hintBox.add(hintTitleLabel, BorderLayout.NORTH);
	} else {
	    hintBox.remove(hintTitleLabel);
	}

	revalidate();
	repaint();
    }

    /**
     * Returns whether header is visible.
     * 
     * @return true, if the header is visible, false otherwise.
     */
    public boolean isHeaderVisible() {
	return scrollPane.getColumnHeader().isVisible();
    }

    /**
     * Sets whether header is visible.
     * 
     * @param headerVisible
     *            the desired visibility of header.
     */
    public void setHeaderVisible(boolean headerVisible) {
	scrollPane.getColumnHeader().setVisible(headerVisible);
	repaint();
    }

    /**
     * Returns whether columns are resizable using divider line.
     * 
     * @return true, if columns are resizable, false otherwise.
     */
    public boolean isResizableByDividerLine() {
	return columnResizer.isEnabled();
    }

    /**
     * Sets whether columns are resizable using divider line.
     * 
     * @param resizableDivider
     *            the desired resizability of divider line.
     */
    public void setResizableByDividerLine(boolean resizableDivider) {
	columnResizer.setEnabled(resizableDivider);
    }

    /**
     * Sets labels of columns.
     * 
     * @param nameLabel
     *            the label of column with property names.
     * @param valueLabel
     *            the lable of column with property values.
     */
    public void setHeaderLabels(String nameLabel, String valueLabel) {
	TableColumnModel tcm = propertiesTable.getColumnModel();
	tcm.getColumn(0).setHeaderValue(nameLabel);
	tcm.getColumn(1).setHeaderValue(valueLabel);
	repaint();
    }

    /**
     * Returns background color of composed properties without a type.
     * 
     * @return the color.
     */
    public Color getGroupNameBackground() {
	return groupNameBackground;
    }

    /**
     * Sets background color of composed properties without a type.
     * 
     * @param color
     *            the desired color, or null, if a special background is not
     *            desired.
     */
    public void setGroupNameBackground(Color color) {
	if (this.groupNameBackground == color) {
	    return;
	}

	if ((this.groupNameBackground != null) && (this.groupNameBackground.equals(color))) {
	    return;
	}

	this.groupNameBackground = color;
	repaint();
    }

    /**
     * Returns foreground color of composed properties without a type.
     * 
     * @return the color.
     */
    public Color getGroupNameForeground() {
	return groupNameForeground;
    }

    /**
     * Sets foreground color of composed properties without a type.
     * 
     * @param color
     *            the desired color, or null, if a special foreground is not
     *            desired.
     */
    public void setGroupNameForeground(Color color) {
	if (this.groupNameForeground == color) {
	    return;
	}

	if ((this.groupNameForeground != null) && (this.groupNameForeground.equals(color))) {
	    return;
	}

	this.groupNameForeground = color;
	repaint();
    }

    // -----------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------

    /**
     * Updates row heights.
     */
    private void updateRowHeights() {
	int minCommonHeight = 0;
	FontMetrics metrics = propertiesTable.getFontMetrics(propertiesTable.getFont());
	minCommonHeight = Math.max(minCommonHeight, metrics.getHeight());

	if (expandIcon != null) {
	    minCommonHeight = Math.max(minCommonHeight, expandIcon.getHeight());
	}

	if (collapseIcon != null) {
	    minCommonHeight = Math.max(minCommonHeight, collapseIcon.getHeight());
	}

	propertiesTable.setRowHeight(minCommonHeight + 2);
    }

    /**
     * Clones a buffered image.
     */
    private static BufferedImage deepCopy(BufferedImage image) {
	ColorModel cm = image.getColorModel();
	boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
	WritableRaster raster = image.copyData(null);
	return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
