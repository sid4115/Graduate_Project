package ca.queensu.cs.rover.configuration.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.papyrus.infra.core.sasheditor.contentprovider.IComponentModel;
import org.eclipse.papyrus.infra.core.sasheditor.contentprovider.IPageModel;
import org.eclipse.papyrus.infra.core.sasheditor.editor.ICloseablePart;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.core.utils.ServiceUtils;
import org.eclipse.papyrus.infra.ui.extension.diagrameditor.EditorDescriptor;
import org.eclipse.papyrus.infra.ui.extension.diagrameditor.IPluggableEditorFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import ca.queensu.cs.rover.configuration.IConfigurationPageService;

public class ConfigurationPageFactory implements IPluggableEditorFactory {

	private ServicesRegistry services;

	private ImageDescriptor icon;

	private TransactionalEditingDomain editingDomain;

	@Override
	public void init(ServicesRegistry serviceRegistry, EditorDescriptor editorDescriptor) {
		this.services = serviceRegistry;
		try {
			this.editingDomain = ServiceUtils.getInstance().getTransactionalEditingDomain(serviceRegistry);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		this.icon = editorDescriptor.getIcon();
	}
 
	@Override 
	public IPageModel createIPageModel(Object pageIdentifier) {
		return new ConfigurationPageModel(pageIdentifier);
	}

	@Override
	public boolean isPageModelFactoryFor(Object pageIdentifier) {
		return ConfigurationPageService.isModel(pageIdentifier);
	}

	private class ConfigurationPageModel implements IComponentModel, IAdaptable {
		
		
		private Object model;
		private Image iconImage;

		private ConfigurationPage configuration;
		public ConfigurationPageModel(Object model) {
			super();

			this.model = model;
		}

		@Override
		public String getTabTitle() {
			return "Configuration";
		}

		@Override
		public Image getTabIcon() {
			if ((iconImage == null) && (icon != null)) {
				iconImage = icon.createImage(Display.getCurrent());
			}

			return iconImage;
		}

		@Override
		public Object getRawModel() {
			return model;
		}

		@Override
		public void dispose() {
			if (iconImage != null) {
				iconImage.dispose();
				iconImage = null;
			}
			if(configuration != null)
			{
				configuration.dispose();
			}
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			T result = null;

			if ((adapter == ICloseablePart.class) || (adapter == ConfigurationPage.class)) {
				result = adapter.cast(configuration); // Cast is null-safe
			}

			if (result == null) {
				result = Platform.getAdapterManager().getAdapter(this, adapter);
			}

			return result;
		}

		@Override
		public Composite createPartControl(Composite parent) {
			// creating a new configuration page on the editing domian
			Composite result;

			try {
			 IConfigurationPageService service = services.getService(IConfigurationPageService.class);
			 configuration = new ConfigurationPage(service, model, editingDomain);
			 result = configuration.createControl(parent);
			} catch (ServiceException e) {
				throw new IllegalStateException("Configuration-page service not available", e);
			}

			return result;
		}

	}

}
