package ca.queensu.cs.rover.configuration.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.infra.core.sasheditor.di.contentprovider.IOpenable;

public class ConfigurationAdapterFactory implements IAdapterFactory {

	public ConfigurationAdapterFactory() {
		super();
	}
	
	@Override
	public <T> T getAdapter(Object adaptable, Class<T> adapterType) {
		T result = null;

		if (adaptable instanceof EObject) {
			if (adapterType == IOpenable.class) {
				result = adapterType.cast(new IOpenable.Openable(adaptable));
			}
		}

		return result;
	}
 
	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IOpenable.class };
	}

}
 