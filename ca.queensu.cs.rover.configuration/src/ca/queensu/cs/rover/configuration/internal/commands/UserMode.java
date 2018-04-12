package ca.queensu.cs.rover.configuration.internal.commands;

import java.util.ArrayList;

import java.util.List;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.uml.tools.model.UmlUtils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;

// Thic class is used to set the user mode: Beginner or Expert. Based on the choosen option the flow of control takeS different paths

public class UserMode {

	private List<Class> capsules;
	private Model model = null;

	public List<Class> getCapsules() {

		List<Class> capsules = new ArrayList<Class>();
		EList<NamedElement> list = this.model.getOwnedMembers();

		for(NamedElement member:list)
		{
			if (member instanceof Class) {
				capsules.add((Class) member);
			}
		}
		return capsules;
	}
	private Class getCapsule(String name) {
		Class capsule = null;
		for (Class c: capsules) {
			if (c.getName().equalsIgnoreCase(name)) {
				capsule = c;
				break;
			}
		}
		return capsule;
	}
	public Object execute(String Value) throws ExecutionException {

		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ModelSet modelSet = null;
		final ResourceSet resourceSet = new ResourceSetImpl();

		if (!(editor instanceof PapyrusMultiDiagramEditor)) {
			return null;
		}

		ServicesRegistry services = ((PapyrusMultiDiagramEditor)editor).getServicesRegistry();
		try {
			modelSet = services.getService(ModelSet.class);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(modelSet);

		try {

			this.model = (Model)UmlUtils.getUmlResource(modelSet).getContents().get(0); 
			this.capsules = this.getCapsules();
			Class capsule = this.getCapsule("ControlSoftware");

			editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

				@Override
				protected void doExecute() {
					Property attribute = capsule.getAttribute("mode", null);
					if(attribute != null)
					{
						attribute.setDefault(Value);
					}
				}
			});

		}
		finally {
			EMFHelper.unload(resourceSet);
		}
		return null;

	}
}
