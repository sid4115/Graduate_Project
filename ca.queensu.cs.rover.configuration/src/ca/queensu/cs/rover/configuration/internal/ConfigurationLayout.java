package ca.queensu.cs.rover.configuration.internal;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import ca.queensu.cs.rover.configuration.IConfigurationPageService;

/**
 * @author Sudharshan
 *
 */
class ConfigurationLayout {

	@SuppressWarnings("unused")
	private final Composite parent;
	@SuppressWarnings("unused")
	private final FormToolkit toolkit;
	@SuppressWarnings("unused")
	private final IConfigurationPageService welcomeService;

	/**
	 * Constructor.
	 *
	 */
	ConfigurationLayout(Composite parent, FormToolkit toolkit, IConfigurationPageService welcomeService) {
		super();

		this.parent = parent;
		this.toolkit = toolkit;
		this.welcomeService = welcomeService;
	}

	void resetLayoutModel() {

	}

	void layout() {

	}

}

