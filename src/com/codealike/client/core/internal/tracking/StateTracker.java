package com.codealike.client.core.internal.tracking;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;*/
import com.codealike.client.core.internal.model.*;
import com.codealike.client.intellij.EventListeners.CustomCaretListener;
import com.codealike.client.intellij.EventListeners.CustomDocumentListener;
import com.intellij.compiler.server.BuildManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.BuildInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.model.exception.NonExistingResourceException;
import com.codealike.client.core.internal.services.TrackingService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.tracking.ActivitiesRecorder.FlushResult;
//import com.codealike.client.core.internal.tracking.build.ResourceDeltaVisitor;
import com.codealike.client.core.internal.tracking.code.ContextCreator;
//import com.codealike.client.core.internal.utils.EditorUtils;
import com.codealike.client.core.internal.utils.LogManager;
//import com.codealike.client.core.internal.utils.WorkbenchUtils;

public class StateTracker {

	private ActivitiesRecorder recorder;
	private ActivityState currentState;
	private ActivityState lastState;
	//private final Display display;
	private final Duration idleMinInterval;
	private final int idleDetectionPeriod;
	//private Set<IDocument> registeredDocs;
	protected Document currentCompilationUnit;
	private CodeContext lastCodeContext;
	private ActivityEvent lastEvent;
	private ContextCreator contextCreator;
	private ScheduledThreadPoolExecutor idleDetectionExecutor;

	private DocumentListener documentListener;
	//private CaretListener caretListener;

	/**
	 * Listens to the build events. Since build is automatic by default, this will happen many times while working.
	 */
	/*private final IResourceChangeListener buildEventsListener = new IResourceChangeListener() {
		
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			switch (event.getType()) {
				case IResourceChangeEvent.PRE_BUILD : {
					
					try {
						TrackingService trackingService = PluginContext.getInstance().getTrackingService();
						
						ResourceDeltaVisitor deltaVisitor = new ResourceDeltaVisitor();
						event.getDelta().accept(deltaVisitor);
						for (IProject project : deltaVisitor.getAffectedProjects()) {
							if (!trackingService.isTracked(project)) {
								return;
							}
							UUID projectId = trackingService.getTrackedProjects().get(project);
							ActivityState buildState = ActivityState.createBuildState(projectId);
							recorder.recordState(buildState);
							
							CodeContext context = contextCreator.createCodeContext(project);
							ActivityEvent buildEvent = new BuildActivityEvent(projectId, ActivityType.BuildProject, context);
							recorder.recordEvent(buildEvent);
							
							currentState = buildState;
							lastEvent = buildEvent;
						}
					}
					catch (Exception e) {
						
					}
					
					break;
				}
				case IResourceChangeEvent.POST_BUILD : {
						
					try {
						TrackingService trackingService = PluginContext.getInstance().getTrackingService();
						
						ResourceDeltaVisitor deltaVisitor = new ResourceDeltaVisitor();
						event.getDelta().accept(deltaVisitor);
						for (IProject project : deltaVisitor.getAffectedProjects()) {
							if (!trackingService.isTracked(project)) {
								return;
							}
							
							List<IMarker> problems = EditorUtils.getCompilationErrors(project);
							ActivityEvent buildEvent = null;
							
							CodeContext context = contextCreator.createCodeContext(project);
							
							UUID projectId = trackingService.getTrackedProjects().get(project);
							if (problems != null && !problems.isEmpty()) {
								buildEvent = new BuildActivityEvent(projectId, ActivityType.BuildProjectFailed, context);
								recorder.recordEvent(buildEvent);
								buildEvent = new BuildActivityEvent(projectId, ActivityType.BuildSolutionFailed, context);
							}
							else {
								buildEvent = new BuildActivityEvent(projectId, ActivityType.BuildProjectSucceeded, context);
								recorder.recordEvent(buildEvent);
								buildEvent = new BuildActivityEvent(projectId, ActivityType.BuildSolutionSucceded, context);
							}
							
							recorder.recordEvent(buildEvent);
							lastEvent = buildEvent;
							ActivityState state = ActivityState.createNullState(projectId);
							recorder.recordState(state);
							currentState = state;
						}
					}
					catch (Exception e) {
						LogManager.INSTANCE.logError(e, "Problem recording post build event.");
					}
					
					break;
				}
				default : {};
			}
		}
	};

	private final IDebugEventSetListener debugEventListener = new IDebugEventSetListener() {

		@Override
		public void handleDebugEvents(DebugEvent[] events) {
			for (DebugEvent event : events) {
				handleDebugEvent(event);
			}
		}
	};

	private synchronized void handleDebugEvent(DebugEvent event) {
		ActivityState state = ActivityState.NONE;
		UUID projectId = PluginContext.UNASSIGNED_PROJECT;

		if (event.getSource() instanceof IProcess)
		{
			if (event.getKind() == DebugEvent.TERMINATE 
					&& currentState.getType() == ActivityType.Debugging)
			{
				state = ActivityState.createNullState(projectId);
			}
		}
		if (event.getSource() instanceof IDebugTarget) {
			
			// Records the start time of this launch:
			if (event.getKind() == DebugEvent.CREATE
					&& currentState.getType() != ActivityType.Debugging) {

				state = ActivityState.createDebugState(projectId);
			}
			else if (event.getKind() == DebugEvent.TERMINATE) {
				state = ActivityState.createNullState(projectId);
			}
		} else if (event.getSource() instanceof IThread) {
			if (event.getKind() == DebugEvent.SUSPEND
					&& currentState.getType() != ActivityType.Debugging) {
				state = ActivityState.createDebugState(PluginContext.UNASSIGNED_PROJECT);
			}
			else if (event.getKind() == DebugEvent.RESUME)
			{
				state = ActivityState.createNullState(projectId);
			}
		}

		if (state != null && state != ActivityState.NONE) {
			currentState = state;
			recorder.recordState(state);
		}
	}

	private final IDocumentListener documentListener = new IDocumentListener() {

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			// TODO: do we need to do something?
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			IEditorPart activeEditor = WorkbenchUtils.getActiveEditor();
			trackCodingEvents(activeEditor);
		}

	};
	
	private final ISelectionListener selectionListener = new ISelectionListener() {
		
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part instanceof IEditorPart) {
				trackDocumentFocus(part);
			}
		}
	};
	
	private final IPartListener partsListener = new IPartListener() {

		@Override
		public void partActivated(final IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				trackDocumentFocus(part);
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
			// Do nothing.
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				deregisterPart(part);
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				registerPart(part);
			}
		}
	};


	private synchronized void trackDocumentFocus(final IWorkbenchPart part) {
		if (!(part instanceof IEditorPart)) {
			return;
		}
		
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			
			@Override
			public void run() {
				try {
					TrackingService trackingService = PluginContext.getInstance().getTrackingService();
					IEditorPart editor = (IEditorPart)part;
					IProject activeProject = EditorUtils.getActiveProject(editor);
					
					if (activeProject == null || !trackingService.isTracked(activeProject)) {
						return;
					}
					
					UUID projectId = trackingService.getTrackedProjects().get(activeProject);
	
					if (currentState.getType() != ActivityType.Coding || currentState.getProjectId() != projectId) {
						
						if (currentState.getType() != ActivityType.Debugging) {
							currentState = ActivityState.createDesignState(projectId);
							recorder.recordState(currentState);
						}
					}
					
					IResource focusedResource = EditorUtils.getActiveResource(editor);
					if (focusedResource == null) {
						return;
					}
					trackNewSelection(editor, focusedResource, projectId);
				} catch (NonExistingResourceException e) {
					LogManager.INSTANCE.logError(e, "Problem recording document focus. Cannot find the active resource.");
				} catch (Exception e) {
					LogManager.INSTANCE.logError(e, "Problem recording document focus.");
				}
			}
		});
	}
	*/
	public  void trackNewSelection(Editor editor) {
		FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
		TrackingService trackingService = PluginContext.getInstance().getTrackingService();

		if (editor == null || !trackingService.isTracked(editor.getProject())) {
			return;
		}
		UUID projectId = trackingService.getTrackedProjects().get(editor.getProject());

		StructuralCodeContext currentCodeContext = new StructuralCodeContext(projectId);
		currentCodeContext.setProject(editor.getProject().getName());

		Document focusedResource = editor.getDocument();

		if (!focusedResource.equals(currentCompilationUnit) || !currentCodeContext.equals(lastCodeContext)) {
			ActivityEvent event = new ActivityEvent(projectId, ActivityType.DocumentFocus, currentCodeContext);
			recorder.recordEvent(event);
			
			lastEvent = event;
			currentCompilationUnit = focusedResource;
			lastCodeContext = currentCodeContext;
		}
	}
/*
	private synchronized void trackCodingEvents(final IWorkbenchPart part) {
		if (!(part instanceof IEditorPart)) {
			return;
		}

		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					TrackingService trackingService = PluginContext.getInstance().getTrackingService();
					ActivityEvent event = null;
					IEditorPart editor = (IEditorPart) part;
					IResource focusedResource = EditorUtils.getActiveResource(editor);
					if (focusedResource == null || !trackingService.isTracked(focusedResource.getProject())) {
						return;
					}
					UUID projectId = trackingService.getTrackedProjects().get(focusedResource.getProject());
					
					if (focusedResource.equals(currentCompilationUnit) && lastEvent != null && lastEvent.getType() != ActivityType.DocumentEdit) {
						//Currently in design mode, so we need to save an editing event
						event = new ActivityEvent(projectId, ActivityType.DocumentEdit, contextCreator.createCodeContext(editor, projectId));
						recorder.recordEvent(event);
					}
					
					if (lastEvent != null) {
						lastEvent = event;
					}
				} catch (NonExistingResourceException e) {
					LogManager.INSTANCE.logError(e, "Problem recording document edit. Cannot find the active resource.");
				} catch (Exception e) {
					LogManager.INSTANCE.logError(e, "Problem recording document edit.");
				}
			}
		});
	}
	*/

	public synchronized void trackCodingEvent(Editor editor) {
		try {
			FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
			TrackingService trackingService = PluginContext.getInstance().getTrackingService();
			ActivityEvent event = null;
			if (editor == null || !trackingService.isTracked(editor.getProject())) {
				return;
			}
			UUID projectId = trackingService.getTrackedProjects().get(editor.getProject());

			Document focusedResource = editor.getDocument();
			PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(editor.getProject());

			if (focusedResource.equals(currentCompilationUnit) && lastEvent != null && lastEvent.getType() != ActivityType.DocumentEdit) {
				//Currently in design mode, so we need to save an editing event

				CodeContext context = new StructuralCodeContext(projectId);
				context.setProject(editor.getProject().getName());

				VirtualFile file = fileDocumentManager.getFile(editor.getDocument());
				if (file != null) {
					context.setFile(file.getName());
				}

				PsiJavaFile javaPsiFile = (PsiJavaFile) psiDocumentManager.getPsiFile(editor.getDocument());
				if (javaPsiFile != null) {
					PsiClass[] classes = javaPsiFile.getClasses();
					if (classes.length > 0) {
						context.setClassName(classes[0].getQualifiedName());
					}

					//PsiMember member = javaPsiFile.getManager().getModificationTracker().
					//context.setMemberName();
					context.setPackageName(javaPsiFile.getPackageName());
				}

				event = new ActivityEvent(projectId, ActivityType.DocumentEdit, context);
				recorder.recordEvent(event);
			}

			if (lastEvent != null) {
				lastEvent = event;
			}
		} catch (Exception e) {
			LogManager.INSTANCE.logError(e, "Problem recording document edit.");
		}
	}


	public void startTrackingProject(Project project, UUID projectId, DateTime startWorkspaceDate) {
		ActivityEvent openSolutionEvent = new ActivityEvent(projectId, ActivityType.OpenSolution, contextCreator.createCodeContext(project));
		openSolutionEvent.setCreationTime(startWorkspaceDate);
		ActivityState systemState = ActivityState.createSystemState(projectId);
		systemState.setCreationTime(startWorkspaceDate);
		
		recorder.recordState(systemState);
		recorder.recordEvent(openSolutionEvent);
		ActivityState nullState = ActivityState.createNullState(projectId);
		recorder.recordState(nullState);
		
		currentState = nullState;
	}

	/*private void register(IWorkbenchWindow window) {
		window.getPartService().addPartListener(partsListener);
		window.getSelectionService().addPostSelectionListener(selectionListener);
		for (IWorkbenchPage page : window.getPages()) {
			for (IEditorReference ref : page.getEditorReferences()) {
				IEditorPart editor = ref.getEditor(false);
				registerPart(editor);
			}
		}
	}*/
	
	/*private void deregisterWindow(IWorkbenchWindow window) {
		window.getPartService().removePartListener(partsListener);
		window.getSelectionService().removePostSelectionListener(selectionListener);
		for (IWorkbenchPage page : window.getPages()) {
			for (IEditorReference ref : page.getEditorReferences()) {
				IEditorPart editor = ref.getEditor(false);
				deregisterPart(editor);
			}
		}
	}*/
	
	/*private void registerPart(final IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IDocument doc = EditorUtils.getActiveDocument((IEditorPart)part);
			if (doc != null) {
				doc.addDocumentListener(documentListener);
				if (!registeredDocs.contains(doc)) {
					registeredDocs.add(doc);
				}
			}
		}
	}*/
	
	/*private void deregisterPart(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IDocument doc = EditorUtils.getActiveDocument((IEditorPart)part);
			if (doc != null) {
				registeredDocs.remove(doc);
			}
		}
	}*/

	/**
	 * A window listener listening to window focus.
	 */
	/*private final IWindowListener winListener = new IWindowListener() {

		@Override
		public void windowActivated(IWorkbenchWindow window) {
			ActivityState newState;
			if (lastState != null) {
				newState = lastState.recreate();
			}
			else {
				newState = ActivityState.createIdleState(PluginContext.UNASSIGNED_PROJECT);
			}
			lastState = currentState;
			currentState = newState;
			recorder.recordState(currentState);
//			trackDocumentFocus(window.getPartService().getActivePart());
		}

		@Override
		public void windowClosed(IWorkbenchWindow window) {
			deregisterWindow(window);
		}

		@Override
		public void windowDeactivated(IWorkbenchWindow window) {
			IdleActivityState idle = ActivityState.createIdleState(PluginContext.UNASSIGNED_PROJECT);
			recorder.recordState(idle);
			lastState = currentState;
			currentState = idle;
		}

		@Override
		public void windowOpened(IWorkbenchWindow window) {
			register(window);
			if (window.getWorkbench().getActiveWorkbenchWindow() == window) {
				trackDocumentFocus(window.getPartService().getActivePart());
			}
		}
	};
*/
	public StateTracker(/*Display disp,*/ int idleDetectionPeriod,
			Duration idleMinInterval) {
		this.idleDetectionPeriod = idleDetectionPeriod;
		this.idleMinInterval = idleMinInterval;
		//this.registeredDocs = new HashSet<IDocument>();
		this.contextCreator = PluginContext.getInstance().getContextCreator();
		//display = disp;

		recorder = new ActivitiesRecorder(PluginContext.getInstance());
	}

	public void startTracking() {
		documentListener = new CustomDocumentListener();
		//caretListener = new CustomCaretListener();

		ApplicationManager.getApplication().invokeLater(() -> {
			// edit document
			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.addDocumentListener(documentListener);

			//EditorFactory
			//		.getInstance()
			//		.getEventMulticaster()
			//		.addCaretListener(caretListener);
		});
	}

/*
	private void startIdleDetection() {

		display.syncExec(new Runnable() {

			@Override
			public void run() {
				display.addFilter(SWT.KeyDown, userActivityListener);
				display.addFilter(SWT.MouseDown, userActivityListener);
			}

		});

		this.idleDetectionExecutor = new ScheduledThreadPoolExecutor(1);
		Runnable idlePeriodicTask = new Runnable() {

			@Override
			public void run() {
				synchronized (this) {

					if (currentState.getType() == ActivityType.Idle) {

						if (currentState instanceof NullActivityState) {
							currentState = ActivityState.createIdleState(PluginContext.UNASSIGNED_PROJECT);
							recorder.recordState(currentState);
						}
						else {
							return;
						}

					}
				}
			}
		};
		
		this.idleDetectionExecutor.scheduleAtFixedRate(idlePeriodicTask, idleDetectionPeriod,
				idleDetectionPeriod, TimeUnit.MILLISECONDS);
	}
	
	protected Listener userActivityListener = new Listener() {

		//This listens to activity only inside the ide (probably is useful for activity outside the editor)
		@Override
		public void handleEvent(Event event) {
			if (currentState instanceof IdleActivityState) {
				IdleActivityState state = (IdleActivityState) currentState;
				DateTime now = DateTime.now();
				Duration duration = new Duration(state.getLastActivity(), now);
				state.setLastActivity(now);
				

				if (duration.compareTo(idleMinInterval) < 0) {
					currentState = ActivityState.createIdleState(PluginContext.UNASSIGNED_PROJECT);
					recorder.recordState(currentState);
				}
			}
		}
	};
*/
	public void stopTracking() {
		ApplicationManager.getApplication().invokeLater(() -> {
			// edit document
			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.removeDocumentListener(documentListener);

			//EditorFactory
			//		.getInstance()
			//		.getEventMulticaster()
			//		.removeCaretListener(caretListener);
		});
	}

	public FlushResult flush(String identity, String token) {
		try {
			return this.recorder.flush(identity, token);
		}
		catch (Exception e) {
			LogManager.INSTANCE.logError(e, "Couldn't send data to the server.");
			return FlushResult.Report;
		}
	}

}
