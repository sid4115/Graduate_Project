package ca.queensu.cs.rover.configuration.extensionPoints;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLFactory;

import ca.queensu.cs.rover.configuration.dialogs.AttributeNameDialog;
import ca.queensu.cs.rover.configuration.internal.ConfigurationPage;

public class Camera implements DesignTab {


	/* This is the code to create the configuration page of the camera component
	 * Only those components who are registered with our defined extension point, 
	 * will be made to create configuration pages */

	@Override
	public Composite createSections(Composite parent, TabFolder folder, TabItem tab,
			TransactionalEditingDomain editingDomain, Model model) {
		// Using SWT to create various elements within the page
		Composite comp = new Composite(folder, SWT.None); // Creating a composite panel to place our widgets and data
		GridLayout layout = new GridLayout();
		layout.numColumns= 2;

		comp.setLayout(layout);
		comp.setBackground(new Color(null, 255, 255, 255));

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.minimumWidth = 400;
		gridData.minimumHeight = 250;

		final Group overviewGroup = new Group(comp, SWT.BORDER);
		overviewGroup.setText("Pi-Camera");
		overviewGroup.setLayoutData(gridData);
		overviewGroup.setBackground(new Color(null, 255, 255, 255));
		InputStream rpiData = ConfigurationPage.class.getResourceAsStream("Camera.jpg");
		Display display = Display.getDefault();
		Image rpiImage = new Image(display, rpiData);
		overviewGroup.setLayout(new RowLayout());
		Label label = new Label(overviewGroup, SWT.NONE);
		label.setImage(rpiImage);

		Table table = new Table(comp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);

		Color redColor = new Color(display, 255,250,205);
		Color redColor1 = new Color(display, 0,0,0);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(gridData);
		table.setBackground(redColor);
		table.setForeground(redColor1);
		table.setLinesVisible(true);


		String[] titles = { "PROPERTY", "CAPSULE", "VALUES" };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
			column.setWidth(35);
		}

		Map<String, String> characs = new HashMap<String, String>();
		// Hardware configurations of the Pi-Camera
		characs.put("Still resolution", "8 MegaPixels");
		characs.put("Sensor resolution", "3280 × 2464 pixels");
		characs.put("Video modes", "1080p30, 720p60 and 640 × 480p60/90");
		characs.put("Sensor image area", "3.68 x 2.76 mm (4.6 mm diagonal)");
		characs.put("Pixel size","1.12 µm x 1.12 µm");
		characs.put("Optical size","	1/4\"");



		List<Class> capsules = getCapsules(model);
		// Display all the property attributes via a table on the configuration page. Also add a listener to the table to detect changes
		for(Class capsule: capsules)
		{
			Map<Integer, Property> attributes = this.getAttributes(capsule);

			for(int key : attributes.keySet()) {
				TableEditor editor = new TableEditor(table);
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0, attributes.get(key).getName());
				item.setText(1, capsule.getName());
				final Text text = new Text(table, SWT.CENTER | SWT.BORDER);
				text.setText(String.valueOf(key));
				text.setSize(10, 20);
				text.pack();
				ValueChangeListener changeListener = new ValueChangeListener(capsules,editingDomain, attributes.get(key));
				text.addMouseListener(changeListener);
				editor.minimumWidth= 50;
				editor.horizontalAlignment = SWT.CENTER;
				editor.setEditor(text, item, 2);  
			}
		}

		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}

		Table characTable = new Table(comp, SWT.MULTI);
		characTable.setLinesVisible(false);
		characTable.setHeaderVisible(false);
		characTable.setLayoutData(gridData);
		for (int i = 0; i < 3; i++) {
			TableColumn column = new TableColumn(characTable, SWT.NONE);
			column.setText("");
		}

		for(String key : characs.keySet()) {
			TableItem item = new TableItem(characTable, SWT.NONE);
			item.setText(0, key.toString());
			item.setText(1, ":");
			item.setText(2, characs.get(key));
		}
		for (int i = 0; i < 3; i++) {
			characTable.getColumn(i).pack();
		}

		characTable.getColumn(0).setAlignment(SWT.RIGHT);
		characTable.getColumn(1).setAlignment(SWT.CENTER);
		characTable.getColumn(2).setAlignment(SWT.LEFT);

		tab.setControl(comp);

		return comp;

	}

	/* function to traverse through the UML capsules and retrieve the properties 
	these properties are then in a map collection containing the name and value*/ 
	private Map<Integer, Property> getAttributes(Class capsule) {

		Map<Integer, Property> attributes = new HashMap<Integer, Property>();

		for(Property attribute: capsule.getAllAttributes())
		{
			if(attribute.getType() != null && attribute.getDefault() != null )
			{
				int value = Integer.parseInt(attribute.getDefault());
				attributes.put(value,attribute);
			}
		}
		return attributes;
	}

	// Returns all the capsules from the loaded UML package
	private List<Class> getCapsules(Model model) {
		List<Class> capsules = new ArrayList<Class>();

		for (NamedElement member: this.getRoverPackage(model).getOwnedMembers()) {
			if ((member instanceof Package) && (member.getName().equals("Camera"))) {
				System.out.println("The member name is " + member.getName());
				for (NamedElement innerMember: ((Namespace) member).getOwnedMembers())
				{
					if (innerMember instanceof Class) {
						capsules.add((Class) innerMember);
					}

				}
			}
		}
		return capsules;
	}
	public boolean isRoverPackageLoaded(Model model) {
		return getRoverPackage(model) != null;
	}

	public Package getRoverPackage(Model model) {

		if (model == null)
		{
			return UMLFactory.eINSTANCE.createPackage();
		}
		// Check if the initial package loaded corresponds to beginner mode or expert mode
		Package p1 = (Package)model.getOwnedMember("UMLRT-rover");
		Package p2 = (Package)model.getOwnedMember("UMLRT-Rover");

		Package p = null;

		if(p1 != null)
		{
			p = p1;
		}
		else
		{
			p = p2;
		}
		return p;
	}

	class ValueChangeListener implements MouseListener
	{
		/* Mouse Listener is added so that the user can double click on the table 
		 * in the configuration page and edit the attribute property */ 
		private TransactionalEditingDomain editingDomain;
		private Property attribute;

		public ValueChangeListener(List<Class> capsules, TransactionalEditingDomain editingDomain, Property attribute) {
			this.editingDomain = editingDomain;
			this.attribute = attribute;
		}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub

			Text text = ((Text) e.widget);
			String s = text.getText();


			Display display = Display.getDefault();
			Shell shell = display.getActiveShell();

			AttributeNameDialog dialog = new AttributeNameDialog(shell);
			dialog.setName(s);

			/* get the new values from the dialog box and update the 
			 * new value on the configuration page and the UML model */
			if (dialog.open() == Window.OK) {
				String newName = dialog.getName();
				editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

					@Override
					protected void doExecute() {
						attribute.setDefault(newName);
						text.setText(newName);
					}
				});
			}
		}

		@Override
		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseUp(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
}
