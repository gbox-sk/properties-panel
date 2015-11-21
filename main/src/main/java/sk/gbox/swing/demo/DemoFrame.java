package sk.gbox.swing.demo;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import sk.gbox.swing.propertiespanel.*;
import sk.gbox.swing.propertiespanel.types.*;

@SuppressWarnings("serial")
public class DemoFrame extends JFrame {

    private JPanel contentPane;

    /**
     * View for displaying properties.
     */
    private PropertiesPanel propertiesPanel;

    /**
     * Text area for xml configuration.
     */
    private JTextArea xmlConfigurationArea;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    DemoFrame frame = new DemoFrame();
		    frame.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    /**
     * Creates the frame.
     */
    public DemoFrame() {
	initializeComponents();
	propertiesPanel.setModel(createDemoModel());

	// Load example xml configuration
	try (InputStream is = DemoFrame.class.getResourceAsStream("properties.xml")) {
	    Reader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
	    StringBuilder sb = new StringBuilder();
	    int c;
	    while ((c = reader.read()) != -1) {
		sb.append((char) c);
	    }

	    xmlConfigurationArea.setText(sb.toString());
	} catch (Exception ignore) {

	}
    }

    /**
     * Initializes the model using a code.
     */
    private ComposedProperty createDemoModel() {
	ComposedProperty container = new ComposedProperty();
	container.setLabel("Demo properties");
	container.setHint("Properties created using a code.");

	ComposedProperty propertiesGroup = new ComposedProperty();
	propertiesGroup.setName("properties");
	propertiesGroup.setLabel("Properties");
	propertiesGroup.setImportant(true);
	propertiesGroup.setHint("Basic configuration properties.");

	Property nameProperty = new SimpleProperty(new StringType(), "defaultName");
	nameProperty.setName("name");
	nameProperty.setLabel("name");
	nameProperty.setHint("Name of component.");
	propertiesGroup.getSubproperties().add(nameProperty);

	Property activeProperty = new SimpleProperty(new BooleanType(), "");
	activeProperty.setName("enabled");
	activeProperty.setLabel("enabled");
	activeProperty.setHint("Indicates whether the component is enabled.");
	propertiesGroup.getSubproperties().add(activeProperty);

	Property counterProperty = new SimpleProperty(new IntegerType(0, 100, false), "");
	counterProperty.setName("counter");
	counterProperty.setLabel("counter");
	counterProperty.setHint("Counts something");
	propertiesGroup.getSubproperties().add(counterProperty);

	Property enumProperty = new SimpleProperty(new EnumerationType(Arrays.asList(null,
		"Type A", "Type B", "Type C")), null);
	enumProperty.setName("typeEmpty");
	enumProperty.setLabel("type");
	enumProperty.setHint("Enumaration example with empty value.");
	propertiesGroup.getSubproperties().add(enumProperty);

	ComposedProperty advancedGroup = new ComposedProperty();
	advancedGroup.setName("advanced");
	advancedGroup.setLabel("Advanced properties");
	advancedGroup.setImportant(true);
	advancedGroup.setHint("Advanced configuration properties.");

	Property hintProperty = new SimpleProperty(new StringType(), "");
	hintProperty.setName("hint");
	hintProperty.setLabel("hint");
	hintProperty.setHint("Hint associated with the component.");
	advancedGroup.getSubproperties().add(hintProperty);

	Property typeProperty = new SimpleProperty(new EnumerationType(Arrays.asList("Type A",
		"Type B", "Type C")), "Type B");
	typeProperty.setName("type");
	typeProperty.setLabel("type");
	typeProperty.setHint("Type of something (enumaration).");
	advancedGroup.getSubproperties().add(typeProperty);

	container.getSubproperties().add(propertiesGroup);
	container.getSubproperties().add(advancedGroup);
	return container;
    }

    /**
     * Creates and sets model from xml.
     */
    private void setModelFromXml() {
	// Parse xml configuration
	DocumentBuilder domParser = null;
	try {
	    domParser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(this, "Could not create xml parser.", "Error",
		    JOptionPane.ERROR_MESSAGE);

	    return;
	}

	Document doc = null;
	try {
	    doc = domParser
		    .parse(new InputSource(new StringReader(xmlConfigurationArea.getText())));
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(this, "Could not parse xml configuration.", "Error",
		    JOptionPane.ERROR_MESSAGE);

	    return;
	}

	XmlPropertyBuilder builder = new XmlPropertyBuilder();
	builder.setDefaultPropertyTypeResolver(new DefaultPropertyTypeResolver());

	try {
	    Property property = builder.createProperties(doc);
	    if (property instanceof ComposedProperty) {
		propertiesPanel.setModel((ComposedProperty) property);
	    } else {
		JOptionPane.showMessageDialog(this,
			"Xml configuration configures a simple property.", "Error",
			JOptionPane.ERROR_MESSAGE);
	    }
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(this,
		    "Invalid xml configuration of properties.\n" + e.getLocalizedMessage(),
		    "Error", JOptionPane.ERROR_MESSAGE);
	}
    }

    /**
     * Initializes GUI components.
     */
    private void initializeComponents() {
	setTitle("Demo");
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(100, 100, 604, 477);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	contentPane.setLayout(new BorderLayout(0, 0));
	setContentPane(contentPane);

	JSplitPane splitPane = new JSplitPane();
	splitPane.setResizeWeight(0.5);
	contentPane.add(splitPane, BorderLayout.CENTER);

	JPanel demoPanel = new JPanel();
	splitPane.setRightComponent(demoPanel);
	demoPanel.setLayout(new BorderLayout(0, 0));

	propertiesPanel = new PropertiesPanel();
	demoPanel.add(propertiesPanel);

	JPanel configPanel = new JPanel();
	splitPane.setLeftComponent(configPanel);

	final JCheckBox showHintBoxChB = new JCheckBox("Show hint box");
	showHintBoxChB.setSelected(propertiesPanel.isHintBoxVisible());
	showHintBoxChB.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
		propertiesPanel.setHintBoxVisible(showHintBoxChB.isSelected());
	    }
	});

	final JCheckBox showHintTitleChB = new JCheckBox("Show hint title");
	showHintTitleChB.setSelected(propertiesPanel.isHintTitleVisible());
	showHintTitleChB.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		propertiesPanel.setHintTitleVisible(showHintTitleChB.isSelected());
	    }
	});

	JButton btnGridColor = new JButton("Grid color");
	btnGridColor.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		Color newColor = JColorChooser.showDialog(DemoFrame.this, "Choose grid color",
			propertiesPanel.getGridColor());

		if (newColor != null) {
		    propertiesPanel.setGridColor(newColor);
		}
	    }
	});

	JButton btnTreeColor = new JButton("Tree color");
	btnTreeColor.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		Color newColor = JColorChooser.showDialog(DemoFrame.this,
			"Choose color of tree lines", propertiesPanel.getLineColorOfTree());

		if (newColor != null) {
		    propertiesPanel.setLineColorOfTree(newColor);
		}
	    }
	});

	final JSpinner indentationSpinner = new JSpinner();
	indentationSpinner.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		propertiesPanel.setIndentationLevelShift((int) indentationSpinner.getValue());
	    }
	});
	indentationSpinner.setModel(new SpinnerNumberModel(0, -1, 1, 1));

	JLabel lblNewLabel = new JLabel("Indentation offset:");

	JScrollPane scrollPane = new JScrollPane();

	JButton btnNewButton = new JButton("Create from xml");
	btnNewButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setModelFromXml();
	    }
	});

	JButton btnGroupBackground = new JButton("Background");
	btnGroupBackground.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		Color newColor = JColorChooser.showDialog(DemoFrame.this,
			"Choose background color of groups",
			propertiesPanel.getGroupNameBackground());

		if (newColor != null) {
		    propertiesPanel.setGroupNameBackground(newColor);
		}
	    }
	});

	JButton btnGroupForeground = new JButton("Foreground");
	btnGroupForeground.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		Color newColor = JColorChooser.showDialog(DemoFrame.this,
			"Choose foreground color of groups",
			propertiesPanel.getGroupNameForeground());

		if (newColor != null) {
		    propertiesPanel.setGroupNameForeground(newColor);
		}
	    }
	});

	JLabel lblGroup = new JLabel("Group:");

	GroupLayout gl_configPanel = new GroupLayout(configPanel);
	gl_configPanel
		.setHorizontalGroup(gl_configPanel
			.createParallelGroup(Alignment.LEADING)
			.addGroup(
				gl_configPanel
					.createSequentialGroup()
					.addGroup(
						gl_configPanel
							.createParallelGroup(Alignment.LEADING)
							.addGroup(
								gl_configPanel
									.createSequentialGroup()
									.addContainerGap()
									.addGroup(
										gl_configPanel
											.createParallelGroup(
												Alignment.LEADING)
											.addGroup(
												gl_configPanel
													.createSequentialGroup()
													.addComponent(
														showHintBoxChB)
													.addPreferredGap(
														ComponentPlacement.UNRELATED)
													.addComponent(
														showHintTitleChB))
											.addGroup(
												gl_configPanel
													.createSequentialGroup()
													.addComponent(
														btnGridColor)
													.addPreferredGap(
														ComponentPlacement.RELATED)
													.addComponent(
														btnTreeColor)))
									.addGap(75))
							.addGroup(
								gl_configPanel
									.createSequentialGroup()
									.addContainerGap()
									.addComponent(lblGroup)
									.addPreferredGap(
										ComponentPlacement.RELATED)
									.addComponent(
										btnGroupBackground)
									.addPreferredGap(
										ComponentPlacement.RELATED)
									.addComponent(
										btnGroupForeground))
							.addGroup(
								gl_configPanel
									.createSequentialGroup()
									.addContainerGap()
									.addComponent(lblNewLabel)
									.addPreferredGap(
										ComponentPlacement.RELATED)
									.addComponent(
										indentationSpinner,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
							.addGroup(
								gl_configPanel
									.createSequentialGroup()
									.addContainerGap()
									.addComponent(btnNewButton))
							.addGroup(
								gl_configPanel
									.createSequentialGroup()
									.addGap(12)
									.addComponent(
										scrollPane,
										GroupLayout.DEFAULT_SIZE,
										393,
										Short.MAX_VALUE)))
					.addContainerGap()));
	gl_configPanel.setVerticalGroup(gl_configPanel.createParallelGroup(Alignment.LEADING)
		.addGroup(
			gl_configPanel
				.createSequentialGroup()
				.addContainerGap()
				.addGroup(
					gl_configPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(showHintBoxChB)
						.addComponent(showHintTitleChB))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(
					gl_configPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnGridColor)
						.addComponent(btnTreeColor))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(
					gl_configPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblGroup)
						.addComponent(btnGroupBackground)
						.addComponent(btnGroupForeground))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(
					gl_configPanel
						.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel)
						.addComponent(indentationSpinner,
							GroupLayout.PREFERRED_SIZE,
							GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(btnNewButton)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 265,
					Short.MAX_VALUE).addContainerGap()));

	xmlConfigurationArea = new JTextArea();
	scrollPane.setViewportView(xmlConfigurationArea);
	configPanel.setLayout(gl_configPanel);
    }
}
