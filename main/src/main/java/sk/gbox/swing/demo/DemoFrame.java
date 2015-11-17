package sk.gbox.swing.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import sk.gbox.swing.propertiespanel.*;
import sk.gbox.swing.propertiespanel.types.*;

import javax.swing.JSplitPane;

import java.awt.FlowLayout;

import javax.swing.JCheckBox;

import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class DemoFrame extends JFrame {

    private JPanel contentPane;

    /**
     * View for displaying properties.
     */
    private PropertiesPanel propertiesPanel;

    /**
     * Container (model) for properties.
     */
    private ComposedProperty propertyGroup;

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
	propertyGroup = new ComposedProperty();
	initializeModel();
	propertiesPanel.setModel(propertyGroup);
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
		Color newColor = JColorChooser.showDialog(DemoFrame.this, "Choose Grid Color",
			propertiesPanel.getGridColor());

		if (newColor != null) {
		    propertiesPanel.setGridColor(newColor);
		}
	    }
	});

	JButton btnTreeColor = new JButton("Tree color");
	btnTreeColor.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		Color newColor = JColorChooser.showDialog(DemoFrame.this, "Choose Grid Color",
			propertiesPanel.getLineColorOfTree());

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

	GroupLayout gl_configPanel = new GroupLayout(configPanel);
	gl_configPanel.setHorizontalGroup(
		gl_configPanel.createParallelGroup(Alignment.LEADING)
			.addGroup(gl_configPanel.createSequentialGroup()
				.addContainerGap()
				.addGroup(gl_configPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_configPanel.createSequentialGroup()
						.addComponent(showHintBoxChB)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(showHintTitleChB))
					.addGroup(gl_configPanel.createSequentialGroup()
						.addComponent(btnGridColor)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnTreeColor))
					.addGroup(gl_configPanel.createSequentialGroup()
						.addComponent(lblNewLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(indentationSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap(242, Short.MAX_VALUE))
	);
	gl_configPanel.setVerticalGroup(
		gl_configPanel.createParallelGroup(Alignment.LEADING)
			.addGroup(gl_configPanel.createSequentialGroup()
				.addContainerGap()
				.addGroup(gl_configPanel.createParallelGroup(Alignment.BASELINE)
					.addComponent(showHintBoxChB)
					.addComponent(showHintTitleChB))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_configPanel.createParallelGroup(Alignment.BASELINE)
					.addComponent(btnGridColor)
					.addComponent(btnTreeColor))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(gl_configPanel.createParallelGroup(Alignment.BASELINE)
					.addComponent(lblNewLabel)
					.addComponent(indentationSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(340, Short.MAX_VALUE))
	);
	configPanel.setLayout(gl_configPanel);
    }

    private void initializeModel() {
	ComposedProperty pg1 = new ComposedProperty();
	pg1.setLabel("Visual configuration");
	PropertyType pt = new StringType();
	Property p1 = new SimpleProperty(pt, null);
	p1.setLabel("MinimalWidth");
	p1.setValue("test");
	pg1.getSubproperties().add(p1);
	Property p2 = new SimpleProperty(pt, null);
	pg1.getSubproperties().add(p2);
	p2.setLabel("Timeout");
	p2.setValue("Moj cas");

	ComposedProperty pg2 = new ComposedProperty(pt);
	pg2.setLabel("Data");
	pg2.setImportant(true);

	Property p3 = new SimpleProperty(pt, null);
	p3.setLabel("Name");
	p3.setValue("Moje meno");
	pg2.getSubproperties().add(p3);

	propertyGroup.getSubproperties().add(pg1);
	propertyGroup.getSubproperties().add(pg2);
    }
}
