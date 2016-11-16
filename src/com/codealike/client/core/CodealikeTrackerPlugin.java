/*package com.codealike.client.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.codealike.client.core.api.ApiClient;
import com.codealike.client.core.internal.dto.HealthInfo;
import com.codealike.client.core.internal.dto.HealthInfo.HealthInfoType;
import com.codealike.client.core.internal.services.IdentityService;
import com.codealike.client.core.internal.services.TrackingService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.utils.LogManager;
import com.codealike.client.core.internal.utils.WorkbenchUtils;
import com.codealike.client.core.views.AuthenticationBrowserView;
*/
/**
 * The activator class controls the plug-in life cycle
 */
/*public class CodealikeTrackerPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.codealike.client.eclipse"; //$NON-NLS-1$
	private static final String CODEALIKE_PROPERTIES_FILE = "codealike.properties";

	// The shared instance
	private static CodealikeTrackerPlugin plugin;
	private PluginContext pluginContext;
	private Observer loginObserver = new Observer() {
		
		@Override
		public void update(Observable o, Object arg1) {
			
			if (o == pluginContext.getIdentityService()) {
				TrackingService trackingService = pluginContext.getTrackingService();
				IdentityService identityService = pluginContext.getIdentityService();
				if (identityService.isAuthenticated()) {
					switch(identityService.getTrackActivity()) {
						case Always:
						{
							trackingService.enableTracking();
							break;
						}
						case AskEveryTime:
							//WorkbenchUtils.addMessageToStatusBar("CodealikeApplicationComponent is not tracking your projects");
							break;
						case Never:
							//WorkbenchUtils.addMessageToStatusBar("CodealikeApplicationComponent is not tracking your projects");
							break;
							
					}
				}
				else {
					trackingService.disableTracking();
				}
			}
		}
	};
	*/
	/**
	 * The constructor
	 */
	/*public CodealikeTrackerPlugin() {
	}
*/


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	/*public void start(BundleContext context) throws Exception {
		plugin = this;
		this.pluginContext = PluginContext.getInstance(loadPluginProperties());
		
		try {
			pluginContext.initializeContext();
			super.start(context);
		    
		    if (!pluginContext.checkVersion()) {
		    	throw new Exception();
		    }
		    
		    pluginContext.getTrackingService().setBeforeOpenProjectDate();
		    pluginContext.getIdentityService().addObserver(loginObserver);
			if (!pluginContext.getIdentityService().tryLoginWithStoredCredentials()) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						authenticate();
					}
				});
			}
			else {
				startTracker();
			}
		}
		catch (Exception e)
		{
			ApiClient client = ApiClient.tryCreateNew();
			client.logHealth(new HealthInfo(e, "Plugin could not start.", "eclipse", HealthInfoType.Error, pluginContext.getIdentityService().getIdentity()));
			LogManager.INSTANCE.logError(e, "Couldn't start plugin.");
		}
	}
	
	protected Properties loadPluginProperties() throws IOException {
		Properties properties = new Properties();
		InputStream in = CodealikeTrackerPlugin.class.getResourceAsStream(CODEALIKE_PROPERTIES_FILE);
		properties.load(in);
		in.close();
		
		return properties;
	}
	
	protected void startTracker() {
		pluginContext.getTrackingService().startTracking();
	}

	private void authenticate() {

		AuthenticationBrowserView view = new AuthenticationBrowserView();
		view.showLogin(false);
	}
*/
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	/*public void stop(BundleContext context) throws Exception {
		
		if (pluginContext != null && pluginContext.getTrackingService() != null) {
			pluginContext.getTrackingService().stopTracking(false);
		}
		
		plugin = null;
		super.stop(context);
	}*/

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	/*public static CodealikeTrackerPlugin getDefault() {
		return plugin;
	}*/

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	/*public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
*/