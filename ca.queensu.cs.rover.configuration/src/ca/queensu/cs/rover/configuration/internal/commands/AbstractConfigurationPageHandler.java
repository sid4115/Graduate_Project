package ca.queensu.cs.rover.configuration.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IPage;
import org.eclipse.papyrus.infra.core.sasheditor.editor.ISashWindowsContainer;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import ca.queensu.cs.rover.configuration.IConfigurationPageService;

public abstract class AbstractConfigurationPageHandler extends AbstractHandler {

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);

		if (editor instanceof IMultiDiagramEditor) {
			IMultiDiagramEditor multiEditor = (IMultiDiagramEditor) editor;
			try {
				IConfigurationPageService configurationService = multiEditor.getServicesRegistry().getService(IConfigurationPageService.class);
				doExecute(multiEditor, configurationService);
			} catch (ServiceException e) {
				
				throw new ExecutionException("Could not obtain the configuration-page service."); 
			}
		}
		 
		return null;
	}
		
	protected abstract void doExecute(IMultiDiagramEditor editor, IConfigurationPageService configurationService);

	@Override
	public void setEnabled(Object evaluationContext) {
		Object editor = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_EDITOR_NAME);

		if (editor instanceof IMultiDiagramEditor) {
			IMultiDiagramEditor multiEditor = (IMultiDiagramEditor) editor;
			ISashWindowsContainer sashContainer = multiEditor.getAdapter(ISashWindowsContainer.class);
			if ((sashContainer != null) && !sashContainer.isDisposed()) {
				try {
					@SuppressWarnings("unused")
					IConfigurationPageService configurationService = multiEditor.getServicesRegistry().getService(IConfigurationPageService.class);
				} catch (ServiceException e) {
				}
			}
		}
		setBaseEnabled(true);
	}
	
	protected boolean isEnabled(IMultiDiagramEditor editor, IPage activePage, IConfigurationPageService configurationService) {
		return true;
	}
}
