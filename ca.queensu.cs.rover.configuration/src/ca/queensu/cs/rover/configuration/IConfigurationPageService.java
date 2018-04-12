package ca.queensu.cs.rover.configuration;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.infra.core.services.IService;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;

/**
 * The Rover Editor's <em>Configuration Page</em> management service.
 */
public interface IConfigurationPageService extends IService {

	/**
	 * Obtains the service registry that owns me.
	 * 
	 * @return my registry
	 */
	ServicesRegistry getOwner();
	
	/**
	 * Ensures that the configuration page is open. If it is not open, then it is
	 * opened and activated. Otherwise, if it is not active, it is activated.
	 * @param configurationService 
	 * @param editor 
	 */
	void openConfigurationPage(IMultiDiagramEditor editor, IConfigurationPageService configurationService);
	

	/**
	 * Queries whether the configuration page can be closed at this instant.
	 * 
	 * @return whether the configuration page may be closed by the user
	 */
	boolean canCloseConfigurationPage();
	
	/**
	 * Obtains the Configuration model for the current editor.
	 * 
	 * @return the Configuration model
	 */
	EObject getConfiguration();
}
