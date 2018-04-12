package ca.queensu.cs.rover.configuration.extensionPoints;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.uml2.uml.Model;


public interface DesignTab  {
	
	// This interface is defined as a blue print for the component configuration pages (for instance the camera)  
	
	Composite createSections(Composite parent, TabFolder folder, TabItem tab, TransactionalEditingDomain editingDomain, Model model);

}
