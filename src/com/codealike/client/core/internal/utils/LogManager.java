package com.codealike.client.core.internal.utils;


/*import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.WorkbenchLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;*/

@SuppressWarnings("restriction")
public class LogManager {

	public static final LogManager INSTANCE = new LogManager();
    //Logger logger;
    
    public LogManager() {
    	//Bundle bundle = FrameworkUtil.getBundle( LogManager.class );
        //IEclipseContext context = EclipseContextFactory.getServiceContext( bundle.getBundleContext());
        //this.logger = ContextInjectionFactory.make( WorkbenchLogger.class, context );
    }
 
	public void logError(String msg) {
        //logger.error("CodealikeApplicationComponent: "+msg);
    }
	
	public void logError(Throwable t, String msg) {
        //logger.error(t, "CodealikeApplicationComponent: "+msg);
    }
	
	public void logWarn(String msg) {
		//logger.warn("CodealikeApplicationComponent: "+msg);
	}
	
	public void logWarn(Throwable t, String msg) {
		//logger.warn(t, "CodealikeApplicationComponent: "+msg);
	}
	
	public void logInfo(String msg) {
		//logger.info("CodealikeApplicationComponent: "+msg);
	}
}
