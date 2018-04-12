package ca.queensu.cs.rover.configuration.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.papyrus.infra.core.sasheditor.editor.ICloseablePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import ca.queensu.cs.rover.configuration.IConfigurationPageService;
import ca.queensu.cs.rover.configuration.extensionPoints.DesignTab;
import ca.queensu.cs.rover.configuration.internal.commands.LoadRoverPackage;
import ca.queensu.cs.rover.configuration.internal.commands.RemoveRoverPackage;
import ca.queensu.cs.rover.configuration.internal.commands.UserMode;

public class ConfigurationPage implements ICloseablePart, ResourceSetListener {

	public static final String extensionPoint_ID = "ca.queensu.ca.rover.designTabs";
	private final Model model;
	private final CopyOnWriteArrayList<IPropertyListener> propertyListeners = new CopyOnWriteArrayList<>();
	private ConfigurationLayout configurationLayout;
	private TransactionalEditingDomain editingDomain;
	public ResourceSetListener listener;
	private static boolean flag = true;
	private Composite composite;
	private TabFolder folder;

	public ConfigurationPage(IConfigurationPageService service, Object model, TransactionalEditingDomain editingDomain) {
		super();

		if (model instanceof Model) {
			this.model = (Model)model;
		} 
		else
		{
			this.model = null;
			this.editingDomain = editingDomain;
		}

	} 

	void reset() {
		configurationLayout.resetLayoutModel();
	}

	void layout() {
		configurationLayout.layout();
	}

	public static ConfigurationPage getConfigurationPage(Control control) {
		ConfigurationPage result = null;

		for (Control next = control; (result == null) && (next != null); next = next.getParent()) {
			if (next.getData() instanceof ConfigurationPage) {
				result = (ConfigurationPage) next.getData();
			}
		}
		return result;
	}

	void fireCanCloseChanged() {
		propertyListeners.forEach(l -> {
			try {
				l.propertyChanged(ConfigurationPage.this, PROP_CAN_CLOSE);
			} catch (Exception e) {
				System.out.println(e);
			}
		});
	}

	@Override
	public void addPropertyListener(IPropertyListener listener) {
		propertyListeners.addIfAbsent(listener);
	}

	@Override
	public void removePropertyListener(IPropertyListener listener) {
		propertyListeners.remove(listener);

	}

	@Override
	public boolean canClose() {
		// TODO Auto-generated method stub



		return true;
	}

	// function to extract all the sub-packages from the root UML package
	public ArrayList<Package> getAllNestedPackages(Package rootPackage, ArrayList<Package> list) {
		for (Package subPackage : rootPackage.getNestedPackages()) {
			if (subPackage.getPackagedElements().size() >= 0 && subPackage.getPackagedElements().get(0) instanceof Collaboration) {
				// this is a protocol
				continue;
			}
			list.add(subPackage);
			list = getAllNestedPackages(subPackage, list);
		}
		return list;
	}

	@SuppressWarnings("static-access")
	public Composite createControl(Composite parent) {


		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(extensionPoint_ID);


		this.composite = new Composite(parent, SWT.None);
		composite.setLayout(new FillLayout());
		this.folder =new TabFolder(composite, SWT.NONE);

		ArrayList<Package> packageList = getAllNestedPackages(model, new ArrayList<Package>());

		/* For each UML package that is loaded, check if it is registered with the extension point
           if there a registered package with the extension point create the corresponding configuration pages */
		for (Package p : packageList) {
			for (IConfigurationElement e : config) {

				if ((p.getName()).equals(e.getAttribute("Package_Name"))) {

					try {
						System.out.println(p.getName());
						TabItem tab = new TabItem(folder, SWT.NONE);
						tab.setText(p.getName());
						final Object o = e.createExecutableExtension("Design_Page");
						if (o instanceof DesignTab) { 
							((DesignTab) o).createSections(composite,folder,tab,editingDomain, model);
						}
					}
					catch (CoreException ex) {
						System.out.println(ex.getMessage());
					}
				}

			}
		}
		// Adding a reseource set liestener to the editing domain. This will listen and notify all the changes in the model explorer
		this.editingDomain.addResourceSetListener(this);

		if(flag)
		{

			Display.getDefault().asyncExec(new Runnable() {

				// function containing the dialogues when the tool is first launched
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Display display = Display.getDefault();
					Shell shell = display.getActiveShell();

					MessageDialog dialog = new MessageDialog(shell, "Welcome to Papyrus-RT !!!", null,
							"Are you an Expert User or a Beginner ?", MessageDialog.CONFIRM, new String[] { "Expert",
					"Beginner" }, 0);
					int result = dialog.open();

					if(result ==1)
					{
						boolean res =
								MessageDialog.openConfirm(shell, "Do you want to continue with the Custom-Built Line Follower Scenario ?", "Please confirm");

						if (res){
							RemoveRoverPackage rp = new RemoveRoverPackage();
							try {
								rp.execute();
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							MessageDialog dialog1 = new MessageDialog(shell, "Mode Selection", null,
									"Please Choose the Mode", MessageDialog.INFORMATION, new String[] { "Power Mode",
							"Energy Saving" }, 0);

							int result1 = dialog1.open();
							UserMode mode = new UserMode();
							if(result1 == 0)
							{ 
								try {
									mode.execute("0");
								} 
								catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							else 
							{
								try {
									mode.execute("1");
								} 
								catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						else
						{
							MessageDialog.openInformation(shell, "Info", "Please Re-Launch the application");
						}
					}
					else if (result == 0)
					{
						String [] elements = {"EngineController","DetectionSensor","Camera","LightSensor","LineFollower"};

						ListSelectionDialog dialog1 = 
								new ListSelectionDialog(shell, elements, ArrayContentProvider.getInstance(),
										new LabelProvider(), "Please select the packages to be imported in model explorer");

						dialog1.setTitle("Registered Packages");
						dialog1.setInitialSelections(new Object []{elements});
						dialog1.open();

						Object [] result1 = dialog1.getResult();
						for(Object obj : result1)
						{
							LoadRoverPackage ld = new LoadRoverPackage();

							try {

								ld.execute((String) obj);
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}

				}

			});
			this.flag = false;
		}
		return composite;

	}

	public void dispose()
	{
		this.editingDomain.removeResourceSetListener(this);
	}

	@Override
	public NotificationFilter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAggregatePrecommitListener() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPostcommitOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrecommitOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resourceSetChanged(ResourceSetChangeEvent event) {

		/*The resource set listener notifies all the changes at the model explorer level
		 We filter through all the add/remove notification and detect if a UML package is loaded or un-loaded
		 post this, we create the corresponding configuration page
		 for all the packages that are registered with the extension point */

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(extensionPoint_ID);
		List<Notification> myArray = new ArrayList<Notification>();
		myArray = event.getNotifications();

		for(Notification notif : myArray)

		{
			if(notif.getEventType() == Notification.ADD)
			{
				Object obj = notif.getNewValue();

				if((obj instanceof Package))
				{
					List<Package> pkgList = getAllNestedPackages((Package) obj, new ArrayList<Package>());
					List<String> packageNames = new ArrayList<String>();
					for(Package p : pkgList)
					{
						packageNames.add(p.getName());
					}
					packageNames.add(((Package) obj).getName());
					for (IConfigurationElement e : config) {


						for(String pckName : packageNames)
						{
							if(pckName.equals(e.getAttribute("Package_Name")))
							{

								try {
									System.out.println("pckName" +pckName);
									TabItem tab = new TabItem(folder, SWT.NONE);
									tab.setText(pckName);
									final Object o = e.createExecutableExtension("Design_Page");
									if (o instanceof DesignTab) {
										((DesignTab) o).createSections(composite,folder,tab,editingDomain, model);
									}

								}

								catch (CoreException ex) {
									System.out.println(ex.getMessage());
								}
							}

						}
					}
				}
			}

			/* when the listener gets a remove notification, i.e, a package is un-loaded, 
			 * we remove the configuration pages of that particular package */

			if(notif.getEventType() == Notification.REMOVE)
			{
				Object obj = notif.getOldValue();
				if (obj instanceof Package)                                   
				{
					Package unloadedPackage = (Package) obj;
					String unloadedPackageName = unloadedPackage.getName();
					int size = folder.getItems().length;
					TabItem[] arr = new TabItem[size];
					arr = folder.getItems();
					for(int i =0; i<arr.length;i++)
					{
						if(arr[i].getText().equalsIgnoreCase(unloadedPackageName))
						{
							System.out.println("The Tab Item is being disposed, because the corresponding package is unloaded!!");
							arr[i].dispose();
						}
					}

				}

			}
		}
	}

	@Override
	public Command transactionAboutToCommit(ResourceSetChangeEvent arg0) throws RollbackException {
		// TODO Auto-generated method stub
		return null;
	}

}
