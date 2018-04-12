package ca.queensu.cs.rover.configuration.internal.commands;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;

import ca.queensu.cs.rover.configuration.Activator;

public class RemoveRoverPackage {


	public Object execute() throws ExecutionException {

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
			Resource UmlResource = null;
			String finalPath = "/Package/test/test.uml";
			//String finalPath = "/Package/Rover/test.uml";
			UmlResource = loadTemplateResource(finalPath, resourceSet);
			EcoreUtil.resolveAll(UmlResource);

			// copy all elements
			EcoreUtil.Copier copier = new EcoreUtil.Copier();
			Collection<EObject> umlObjects = copier.copyAll(UmlResource.getContents());
			copier.copyReferences();
			final EList<EObject> contents = UmlUtils.getUmlResource(modelSet).getContents();		
			final Model model = (Model)UmlUtils.getUmlResource(modelSet).getContents().get(0);
			final Package uml = model.getNestedPackage("UMLRT-Rover"); 
			editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

				@Override
				protected void doExecute() {
					// Removing Top capsule from the run-time instance
					((org.eclipse.uml2.uml.Class)model.getPackagedElement("Top")).destroy();
					// Removing the UMLRT-Rover from the run-time instance
					uml.destroy();
					// Removing Control Software Capsule from the run-time instance
					((org.eclipse.uml2.uml.Class)model.getPackagedElement("ControlSoftware")).destroy();

					for (EObject umlObject: umlObjects) {
						if (umlObject instanceof Model) {
							final Package uml = ((Package) umlObject).getNestedPackage("UMLRT-rover");
							final PackageableElement topCls = ((Model) umlObject).getPackagedElement("Top");
							final PackageableElement contSwCls = ((Model) umlObject).getPackagedElement("ControlSoftware");
							model.getPackagedElements().add((PackageableElement) uml);
							model.getPackagedElements().add((PackageableElement) topCls);
							model.getPackagedElements().add((PackageableElement) contSwCls);		  
						}
						else
							contents.add(umlObject);
					}
				}
			});


		}
		finally {
			EMFHelper.unload(resourceSet);
		}
		return null;
	}

	/**
	 * Load template resource.
	 *
	 * @param path
	 *        the path
	 * @return the resource
	 */
	private Resource loadTemplateResource(String path, ResourceSet resourceSet) {
		java.net.URL templateURL = Platform.getBundle(Activator.PLUGIN_ID).getResource(path);
		if(templateURL != null) {
			String fullUri = templateURL.getPath();
			URI uri = URI.createPlatformPluginURI(Activator.PLUGIN_ID + fullUri, true);
			Resource resource = resourceSet.getResource(uri, true);
			if(resource.isLoaded()) {
				return resource;
			}
		}
		return null;
	}

	/**
	 * Obtains the name of the Top capsule.
	 * 
	 * @param root
	 *            - The model's root {@link Element}.
	 * @return The name of the Top capsule.
	 */
	public static String getTopCapsuleName(Element root) {
		String retVal = null;

		EAnnotation anno = root.getEAnnotation("UMLRT_Default_top");
		if (anno != null) {
			retVal = anno.getDetails().get("top_name");
		}

		return retVal != null ? retVal : "Top";
	}


}
