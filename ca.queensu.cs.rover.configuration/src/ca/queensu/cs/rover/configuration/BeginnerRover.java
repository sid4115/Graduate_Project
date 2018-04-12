package ca.queensu.cs.rover.configuration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
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
import org.eclipse.uml2.uml.Model;

import ca.queensu.cs.rover.configuration.extensionPoints.DesignTab;
import ca.queensu.cs.rover.configuration.internal.ConfigurationPage;
import ca.queensu.cs.rover.configuration.internal.commands.UserMode;

public class BeginnerRover implements DesignTab{

	// This class creates the configuration page for the beginner mode, alternatively the same code is called for the expert mode
	public static final String extensionPoint_ID = "ca.queensu.ca.rover.designTabs";

	@Override
	public Composite createSections(Composite parent, TabFolder folder, TabItem tab,
			TransactionalEditingDomain editingDomain, Model model) {

		// Using SWT to create various elements within the page
		Display display = Display.getDefault();
		GridLayout layout = new GridLayout();
		layout.numColumns= 2;
		Composite comp = new Composite(folder, SWT.None);
		comp.setLayout(layout);

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.minimumWidth = 480;
		gridData.minimumHeight = 340;


		final Group overviewGroup = new Group(comp, SWT.BORDER);

		overviewGroup.setText("Platform");
		overviewGroup.setLayoutData(gridData);
		overviewGroup.setBackground(new Color(null, 255, 255, 255));


		InputStream rpiData = ConfigurationPage.class.getResourceAsStream("RaspberryPi.jpg");

		Image rpiImage = new Image(display, rpiData);
		overviewGroup.setLayout(new RowLayout());
		Label label = new Label(overviewGroup, SWT.NONE);

		label.setImage(rpiImage);

		GridData newgridData = new GridData();
		newgridData.grabExcessHorizontalSpace = true;
		newgridData.grabExcessVerticalSpace = true;
		newgridData.minimumWidth = 480;
		newgridData.minimumHeight = 340;


		final Group newOverviewGroup = new Group(comp, SWT.BORDER);

		newOverviewGroup.setText("Polarsys Rover");
		newOverviewGroup.setLayoutData(newgridData);
		newOverviewGroup.setBackground(new Color(null, 255, 255, 255));


		InputStream newData = ConfigurationPage.class.getResourceAsStream("nw.jpg");

		Image newImage = new Image(display, newData);
		newOverviewGroup.setLayout(new RowLayout());
		Label newlabel = new Label(newOverviewGroup, SWT.NONE);

		newlabel.setImage(newImage);
		Map<String, String> characs = new HashMap<String, String>();
		// Raspberry-Pi Configurations
		characs.put("CPU Core", "Quad-core ARM v8, 64Bit");
		characs.put("Name", "Raspberry Pi 3 Model B");
		characs.put("Clock Speed", "1.2GHz");
		characs.put("RAM", "1 GB");
		characs.put("GPU","400 MHz VideoCore IV® 3-D Graphics");
		characs.put("LAN Settings","802.11n Wireless LAN");
		characs.put("GPIOs", "2 x 20 Pin Header");
		characs.put("Power Supply", "2 Ampere @ 5 Volts");

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
			item.setText(0, key);
			item.setText(1, ":");
			item.setText(2, characs.get(key));
		}

		for (int i = 0; i < 3; i++) {
			characTable.getColumn(i).pack();
		}
		characTable.getColumn(0).setAlignment(SWT.RIGHT);
		characTable.getColumn(1).setAlignment(SWT.CENTER);
		characTable.getColumn(2).setAlignment(SWT.LEFT);

		characTable.setLayoutData(gridData);

		Button bt = new Button(comp, SWT.CHECK);
		bt.setText("Change Operation Mode");
		// Listener to change the operational mode. When the Energy Saving mode is selected, the corresponding pages of Power mode gets un-loaded
		bt.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (bt.getSelection())
				{
					int size = folder.getItems().length;
					TabItem[] arr = new TabItem[size];
					arr = folder.getItems();
					for(int i =0; i<arr.length;i++)
					{
						if(arr[i].getText().equals("Camera")) // Camera component not used, hence the page is un-loaded
						{
							Shell shell = display.getActiveShell();
							System.out.println("The Tab Item is being disposed, because the corresponding package is unloaded!!");
							MessageDialog.openInformation(shell, "Information", "Switching the operation Mode to Energy Saving");
							arr[i].dispose();
						}
					}
					UserMode mode = new UserMode();
					try {
						mode.execute("1");
					} catch (ExecutionException e1) {
						e1.printStackTrace();
					}
				}
				else
				{
					IExtensionRegistry registry = Platform.getExtensionRegistry();
					IConfigurationElement[] config = registry.getConfigurationElementsFor(extensionPoint_ID);
					String pckName="Camera";
					for (IConfigurationElement ce : config) {

						if(pckName.equals(ce.getAttribute("Package_Name")))
						{

							try {
								UserMode mode = new UserMode();
								TabItem tab = new TabItem(folder, SWT.NONE);
								tab.setText(pckName);
								final Object o = ce.createExecutableExtension("Design_Page");
								// For each registered package with the extension point, create the corresponding configuration pages
								if (o instanceof DesignTab) { 
									((DesignTab) o).createSections(parent,folder,tab,editingDomain, model);
								}
								mode.execute("0");

							}

							catch (CoreException ex) {
								System.out.println(ex.getMessage());
							} catch (ExecutionException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}

			}
		});
		tab.setControl(comp);
		return comp;
	}

}
