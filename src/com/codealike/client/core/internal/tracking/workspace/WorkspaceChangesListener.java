/*package com.codealike.client.eclipse.internal.tracking.workspace;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.codealike.client.eclipse.internal.startup.PluginContext;
import com.codealike.client.eclipse.internal.tracking.build.ResourceDeltaVisitor;
import com.codealike.client.eclipse.internal.utils.LogManager;

public class WorkspaceChangesListener implements IResourceChangeListener {

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		switch (event.getType()) {
			case IResourceChangeEvent.POST_CHANGE : {
				try {
					ResourceDeltaVisitor deltaVisitor = new ResourceDeltaVisitor(IResourceDelta.ADDED | IResourceDelta.CHANGED, IResourceDelta.OPEN);
					event.getDelta().accept(deltaVisitor);
					for (IProject project : deltaVisitor.getAffectedProjects()) {
						new StartTrackingJob(project).schedule();
					}
				}
				catch (Exception e) {
					LogManager.INSTANCE.logError(e, "Could not start tracking new projects.");
				}
			}
			case IResourceChangeEvent.PRE_DELETE : {
				stopTrackingIfProject(event);
			}
			case IResourceChangeEvent.PRE_CLOSE : {
				stopTrackingIfProject(event);
			}
		}
	}

	private void stopTrackingIfProject(IResourceChangeEvent event) {
		try {
			if (event.getResource() != null && event.getResource().getType() == IResource.PROJECT) {
				PluginContext.getInstance().getTrackingService().stopTracking(event.getResource().getProject());
			}
		}
		catch (Exception e) {
			LogManager.INSTANCE.logError(e, "Could not stop tracking project: "+event.getResource().getProject().getName());
		}
	}
	
	public class StartTrackingJob extends WorkspaceJob {
		
		private IProject project;
		private PluginContext context;

		public StartTrackingJob(IProject project) {
			super("Start tracking job");
			this.project = project;
			this.context = PluginContext.getInstance();
			this.context.getTrackingService().setBeforeOpenProjectDate();
		}
		
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			context.getTrackingService().startTracking(project);
			return Status.OK_STATUS;
		}
		
	}

}
*/