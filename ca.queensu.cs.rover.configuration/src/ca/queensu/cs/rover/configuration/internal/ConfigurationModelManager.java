package ca.queensu.cs.rover.configuration.internal;

import java.nio.file.Path;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.infra.core.resource.ModelSet;

public class ConfigurationModelManager {

	public ConfigurationModelManager(Path stateLocation) {
		super();
	}


	public EObject getConfiguration(ResourceSet resourceSet) {
		Resource resource = getConfigurationResource(resourceSet);
		return (resource == null) ? null : getConfiguration(resource);
	}

	public Resource getConfigurationResource(ResourceSet resourceSet) {
		return null;
	}

	static EObject getConfiguration(Resource resource) {
		return (EObject) EcoreUtil.getObjectByType(resource.getContents(), EcorePackage.Literals.EOBJECT);
	}

	public void connect(ModelSet resourceSet) {
	}


	public void onConfigurationChanged(ModelSet ownerModelSet, Consumer<? super EObject> configurationChangedHandler) {
	}
}
