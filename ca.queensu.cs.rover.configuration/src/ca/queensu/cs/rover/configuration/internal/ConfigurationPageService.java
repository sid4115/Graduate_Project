package ca.queensu.cs.rover.configuration.internal;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Spliterator;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.resource.sasheditor.SashModelUtils;
import org.eclipse.papyrus.infra.core.sasheditor.di.contentprovider.utils.IPageUtils;
import org.eclipse.papyrus.infra.core.sasheditor.editor.DefaultPageLifeCycleEventListener;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IComponentPage;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IEditorPage;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IPage;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IPageLifeCycleEventsListener;
import org.eclipse.papyrus.infra.core.sasheditor.editor.IPageVisitor;
import org.eclipse.papyrus.infra.core.sasheditor.editor.ISashWindowsContainer;
import org.eclipse.papyrus.infra.core.sashwindows.di.PageRef;
import org.eclipse.papyrus.infra.core.sashwindows.di.SashModel;
import org.eclipse.papyrus.infra.core.sashwindows.di.SashWindowsMngr;
import org.eclipse.papyrus.infra.core.sashwindows.di.TabFolder;
import org.eclipse.papyrus.infra.core.sashwindows.di.service.IPageManager;
import org.eclipse.papyrus.infra.core.sashwindows.di.util.PageRemovalValidator;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.core.utils.TransactionHelper;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.tools.util.PlatformHelper;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.papyrus.infra.ui.services.EditorLifecycleEventListener;
import org.eclipse.papyrus.infra.ui.services.EditorLifecycleManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.uml2.uml.Model;

import ca.queensu.cs.rover.configuration.Activator;
import ca.queensu.cs.rover.configuration.Configuration;
import ca.queensu.cs.rover.configuration.IConfigurationPageService;
import ca.queensu.cs.rover.configuration.internal.ConfigurationPage;

public class ConfigurationPageService implements IConfigurationPageService {

	private ServicesRegistry services;
	private ModelSet modelSet;
	private ISashWindowsContainer sashContainer;
	private ConfigurationModelManager configurationManager;
	private IPageManager pageManager;
	private EditorLifecycleManager editorManager;
	private IPageLifeCycleEventsListener sashListener;
	private EditorLifecycleEventListener editorListener;
	private IPage configurationPage;
	private PapyrusMultiDiagramEditor multiEditor;

	@Override
	public void init(ServicesRegistry servicesRegistry) throws ServiceException {
		this.services = servicesRegistry;
	}

	@Override
	public final ServicesRegistry getOwner() {
		return services;
	}


	@Override
	public void openConfigurationPage(IMultiDiagramEditor editor, IConfigurationPageService configurationService) {
		this.multiEditor = (PapyrusMultiDiagramEditor)editor;

		if (pageManager != null) {
			if (configurationPage == null) {
				pageManager.openPage(getModel());
			} else {
				pageManager.selectPage(getModel());
			}
		} 
	}

	void checkConfigurationPage() {
		Display.getDefault().asyncExec(() -> {
			// Check that the editor hasn't been disposed in the mean-time
			if ((sashContainer != null) && !sashContainer.isDisposed()) {
				if (getConfigurationPageCount() <= 0) {
					//	openConfigurationPage();
				}
			}
		});
	} 

	static boolean isModel(Object object) {
		return object instanceof EObject;
	}

	boolean isConfigurationPage(IPage page) {
		return isModel(IPageUtils.getRawModel(page));
	}

	void trackActivePage(PageRef pageRef) {
		TabFolder folder = pageRef.getParent();

		if ((folder != null) && (folder.getCurrentSelection() != pageRef)) {
			SashModel sashModel = EMFHelper.getContainer(folder, SashModel.class);
			if ((sashModel != null) && sashModel.isRestoreActivePage()) {
				// track the active page in this folder
				EditingDomain domain = EMFHelper.resolveEditingDomain(sashModel);

				try {
					TransactionHelper.run(domain, () -> folder.setCurrentSelection(pageRef));
				} catch (Exception e) {
					//	Activator.log.error("Failed to track page selection", e); //$NON-NLS-1$
				}
			}
		}
	}

	void initializeActivePages() {
		SashModel sashModel = getSashModel();
		if ((sashModel != null) && sashModel.isRestoreActivePage()) {
			// Select all of the remembered pages to make them active
			sashContainer.getIFolderList().stream()
			.filter(f -> f.getRawModel() instanceof TabFolder)
			.forEach(f -> {
				TabFolder tabFolder = (TabFolder) f.getRawModel();
				if (tabFolder.getCurrentSelection() != null) {
					IPage page = sashContainer.lookupModelPage(tabFolder.getCurrentSelection());
					if (page != null) {
						sashContainer.selectPage(page);
					}
				}
			});
		}
	}


	private SashModel getSashModel() {
		// Resource may have been unloaded and removed
		SashWindowsMngr sashMngr = (modelSet == null) ? null : SashModelUtils.getSashWindowsMngr(modelSet);
		return (sashMngr == null) ? null : sashMngr.getSashModel();
	}

	private void installPageRemovalValidator() {
		SashModel sashModel = getSashModel();

		if (sashModel != null) {
			sashModel.eAdapters().add(new CloseValidator());
		}
	}

	private void uninstallPageRemovalValidator() {
		SashModel sashModel = getSashModel();

		if (sashModel != null) {
			sashModel.eAdapters().removeIf(CloseValidator.class::isInstance);
		}
	}

	Optional<ConfigurationPage> getConfigurationPage() {
		return Optional.ofNullable(PlatformHelper.getAdapter(configurationPage, ConfigurationPage.class));
	}

	@Override
	public boolean canCloseConfigurationPage() {
		return true;
	}

	public int getConfigurationPageCount() {
		class PageCounter implements IPageVisitor {
			int count = 0;

			@Override
			public void accept(IEditorPage page) {
				count++;
			}

			@Override
			public void accept(IComponentPage page) {
				count++;
			}
		}

		PageCounter counter = new PageCounter();

		sashContainer.visit(counter);

		return counter.count;
	}

	void handleConfigurationChanged(EObject configuration) {
		getConfigurationPage().ifPresent(ConfigurationPage::layout);
	}


	@Override
	public void startService() throws ServiceException {
		configurationManager = Activator.getDefault().getConfigurationManager();
		modelSet = services.getService(ModelSet.class);
		configurationManager.connect(modelSet);   // Calling a Config Model Manager
		configurationManager.onConfigurationChanged(modelSet, this::handleConfigurationChanged); 
		pageManager = services.getService(IPageManager.class);
		editorManager = services.getService(EditorLifecycleManager.class);
		System.out.println(services.getService(IConfigurationPageService.class));
		editorListener = new EditorListener();
		editorManager.addEditorLifecycleEventsListener(editorListener);

		installPageRemovalValidator();
	}

	@Override
	public void disposeService() throws ServiceException {
		uninstallPageRemovalValidator();

		if (configurationManager != null) {
			// FIXME: finish it
		}

		modelSet = null;
		pageManager = null;

		if (editorManager != null) {
			editorManager.removeEditorLifecycleEventsListener(editorListener);
			editorManager = null;
		}
	}

	Model getModel() {
		return getConfiguration();
	}


	@Override
	public Model getConfiguration() {

		if (this.multiEditor == null)
		{
			return null;
		}
		else
		{
			// Get the root node in the workspace
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			// Get the absolute path (file:/...)
			IPath absolutePath = ((FileEditorInput)this.multiEditor.getEditorInput()).getPath();
			// Get the relative project path
			IPath projectPath = root.getFileForLocation(absolutePath).getProject().getFullPath();
			// Get the relative model path 
			IPath modelPath = root.getFileForLocation(absolutePath).getProjectRelativePath();
			// Concatenate the project and model paths to create a platform resource URI (platform:/resource/projectPath/modelPath)
			URI emfUri = URI.createPlatformResourceURI(projectPath.toString() + "/" + modelPath.toString(), true);
			// Remove the "di" extension and replace it with the "uml" one
			emfUri = emfUri.trimFileExtension().appendFileExtension("uml");
			// Get the Resource from the model set. As the URI is now a platform resource URI, 
			// the "RootElement" duplication error in the model explorer no longer exists
			Resource umlResource = this.modelSet.getResource(emfUri, true);
			// Get the model
			Model model = (Model)umlResource.getContents().get(0);
			return model;
		}
	}

	/** Get the welcome model stored in the editor's sash resource, if any. */
	@SuppressWarnings("unused")
	private Configuration getConfigurationOverride() {
		Resource sashResource = SashModelUtils.getSashModel(modelSet).getResource();
		return (sashResource == null)
				? null
						: (Configuration) EcoreUtil.getObjectByType(sashResource.getContents(), EcorePackage.Literals.EOBJECT);
	}

	private class SashListener extends DefaultPageLifeCycleEventListener {

		@Override
		public void pageOpened(IPage page) {
			if (isConfigurationPage(page)) {
				configurationPage = page;
			}

			checkConfigurationPage();
		}

		@Override
		public void pageClosed(IPage page) {
			if (page == configurationPage) {
				configurationPage = null;
			}

			checkConfigurationPage();
		}

		@Override
		public void pageActivated(IPage page) {
			if (page.getRawModel() instanceof PageRef) {
				PageRef pageRef = (PageRef) page.getRawModel();
				trackActivePage(pageRef);
			}
		}
	}


	private class EditorListener implements EditorLifecycleEventListener {

		@Override
		public void postInit(IMultiDiagramEditor editor) {
		}

		@Override
		public void preDisplay(IMultiDiagramEditor editor) {

			sashContainer = editor.getAdapter(ISashWindowsContainer.class);
			sashListener = new SashListener();
			sashContainer.addPageLifeCycleListener(sashListener);

			configurationPage = IPageUtils.lookupModelPage(sashContainer, getModel());

			if (configurationPage == null) {
				configurationPage = IPageUtils.lookupModelPage(sashContainer, configurationManager.getConfiguration(modelSet)); 
			}

			checkConfigurationPage();

			initializeActivePages();
		}

		@Override
		public void postDisplay(IMultiDiagramEditor editor) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeClose(IMultiDiagramEditor editor) {
			sashListener = null;
		}

	}

	private class CloseValidator extends AdapterImpl implements PageRemovalValidator {

		private SashModel sashModel;

		@Override
		public void setTarget(Notifier newTarget) {
			if (newTarget instanceof SashModel) {
				sashModel = (SashModel) newTarget;
			}
		}

		@Override
		public void unsetTarget(Notifier oldTarget) {
			if (oldTarget == sashModel) {
				sashModel = null;
			}
		}

		@Override
		public boolean canRemovePage(PageRef page) {
			@SuppressWarnings("unused")
			Object pageIdentifier = page.getPageIdentifier();
			return true;
		}

		@Override
		public Collection<? extends PageRef> filterRemovablePages(Collection<? extends PageRef> pages) {
			Collection<? extends PageRef> result;
			Optional<? extends PageRef> welcomePage = pages.stream().filter(p -> p.getPageIdentifier() == getModel()).findAny();

			// No problem if the welcome page is not among them
			if (!welcomePage.isPresent()) {
				result = pages;
			} else {
				// If these are all of the pages, then don't close the welcome page
				long allPagesCount = stream(spliteratorUnknownSize(sashModel.eAllContents(), Spliterator.ORDERED), false)
						.filter(PageRef.class::isInstance)
						.count();
				if (allPagesCount > pages.size()) {
					result = pages;
				} else {
					result = new ArrayList<>(pages);
					result.remove(welcomePage.get());
				}
			}

			return result;
		}
	}

}
