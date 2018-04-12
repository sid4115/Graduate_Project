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
import org.eclipse.papyrusrt.umlrt.core.utils.CapsuleUtils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;

import ca.queensu.cs.rover.configuration.Activator;

public class LoadRoverPackage{

	// Thic class is used to load the Rover Package on to the Papyrus-Rt model explorer

	public Object execute(String resourceName) throws ExecutionException {

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
			String path ="/Package/";
			String resourcePath = resourceName +"/"+ resourceName +".uml";
			String finalPath = path + resourcePath;
			System.out.println("Final path is" + finalPath);
			UmlResource = loadTemplateResource(finalPath, resourceSet);
			EcoreUtil.resolveAll(UmlResource);
			
			// copy all elements
			EcoreUtil.Copier copier = new EcoreUtil.Copier();
			Collection<EObject> umlObjects = copier.copyAll(UmlResource.getContents());
			copier.copyReferences();

			// set copied elements in goods resources
			final EList<EObject> contents = UmlUtils.getUmlResource(modelSet).getContents();
			final Model model = (Model)UmlUtils.getUmlResource(modelSet).getContents().get(0);
			final Package uml = model.getNestedPackage("UMLRT-Rover"); 
			editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

				@Override
				protected void doExecute() {


					for (EObject umlObject: umlObjects) {
						if (umlObject instanceof Package) { // check whether the package has the ProtocolContainer stereotypes
							if (((Package) umlObject).getPackagedElements().size() > 0) {
								if (((Package) umlObject).getPackagedElements().get(0) instanceof Collaboration) {
									contents.add(umlObject);
									continue;
								}
							}
							uml.getPackagedElements().add((PackageableElement) umlObject);
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

	@SuppressWarnings("unused")
	private PackageableElement findTopCapsule(String topCapsuleName, Package root) {
		PackageableElement pe = root.getPackagedElement(topCapsuleName);
		if (pe != null && pe instanceof Classifier && CapsuleUtils.isCapsule((Classifier)pe)) {
			return pe;
		}

		EList<Package> nestedPackages = root.getNestedPackages();
		for (int i = 0; i < nestedPackages.size(); i++) {
			Package nestedPackage = nestedPackages.get(i);
			pe = findTopCapsule(topCapsuleName, nestedPackage);
			if (pe != null)
				return pe;
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
