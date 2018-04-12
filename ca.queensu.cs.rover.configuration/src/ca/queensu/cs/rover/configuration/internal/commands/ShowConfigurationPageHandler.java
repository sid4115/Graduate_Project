package ca.queensu.cs.rover.configuration.internal.commands;

import org.eclipse.papyrus.infra.core.sasheditor.editor.IPage;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;

import ca.queensu.cs.rover.configuration.IConfigurationPageService;

public class ShowConfigurationPageHandler extends AbstractConfigurationPageHandler {

	@Override
	protected void doExecute(IMultiDiagramEditor editor, IConfigurationPageService configurationService) {
		configurationService.openConfigurationPage(editor, configurationService);
	}
	
	@Override
	protected boolean isEnabled(IMultiDiagramEditor editor, IPage activePage, IConfigurationPageService configurationService) {
		return true;
	}

}
