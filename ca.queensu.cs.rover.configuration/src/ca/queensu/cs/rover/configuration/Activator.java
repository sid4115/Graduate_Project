package ca.queensu.cs.rover.configuration;

import java.nio.file.Paths;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ca.queensu.cs.rover.configuration.internal.ConfigurationModelManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ca.queensu.cs.rover.configuration"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private ConfigurationModelManager configurationModelManager;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		configurationModelManager = new ConfigurationModelManager(Paths.get(getStateLocation().toOSString())); // Calling a Config Model Manager
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		configurationModelManager = null;
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public ConfigurationModelManager getConfigurationManager() {
		return configurationModelManager;
	}
	
	

}
