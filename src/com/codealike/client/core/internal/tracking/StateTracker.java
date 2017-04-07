package com.codealike.client.core.internal.tracking;


import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import com.codealike.client.core.internal.model.*;
import com.codealike.client.core.internal.utils.TrackingConsole;
import com.codealike.client.intellij.EventListeners.CustomCaretListener;
import com.codealike.client.intellij.EventListeners.CustomDocumentListener;
import com.codealike.client.intellij.EventListeners.CustomEditorMouseListener;
import com.codealike.client.intellij.EventListeners.CustomVisibleAreaListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.services.TrackingService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.tracking.ActivitiesRecorder.FlushResult;
import com.codealike.client.core.internal.tracking.code.ContextCreator;
import com.codealike.client.core.internal.utils.LogManager;

public class StateTracker {

	private ActivitiesRecorder recorder;
	private ActivityState lastState;
	private final Duration idleMinInterval;
	private final int idleDetectionPeriod;
	protected Document currentCompilationUnit;
	private CodeContext lastCodeContext;
	private ActivityEvent lastEvent;
	private ContextCreator contextCreator;
	private ScheduledExecutorService idleDetectionExecutor;

	private DocumentListener documentListener;
	private CaretListener caretListener;
	private VisibleAreaListener visibleAreaListener;
	private CustomEditorMouseListener editorMouseListener;

	private synchronized void populateContext(PsiFile file, CodeContext context, int offset) {
		if (file == null) {
			return;
		}

		// sets file name
		context.setFile(file.getName());

		try {
			// sets the rest of the context based on file type
			switch (file.getFileType().getName()) {
				case "JAVA":
					PsiJavaFile javaPsiFile = (PsiJavaFile) file;
					if (javaPsiFile != null) {
						context.setPackageName(javaPsiFile.getPackageName());

						PsiElement elementAt = javaPsiFile.findElementAt(offset);
						if (elementAt != null) {
							PsiClass elementClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
							if (elementClass != null) {
								context.setClassName(elementClass.getName());
							}

							PsiMember member = PsiTreeUtil.getParentOfType(elementAt, PsiMember.class);
							if (member != null) {
								context.setMemberName(member.getName());
							}
						}
					}
					break;
				case "PLAIN_TEXT":
				case "HTML":
				case "Kotlin":
				case "GUI_DESIGNER_FORM":
					break;
			}
		}
		catch(Exception psiException) {
			// for some reason file was not casted properly to expected format
			LogManager.INSTANCE.logInfo(String.format("Could not track activity in file %s.", context.getFile()));
		}
	}

	public  void trackDocumentFocus(Editor editor, int offset) {
		if (editor == null)
			return;

		try {
			FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
			TrackingService trackingService = PluginContext.getInstance().getTrackingService();

			UUID projectId = trackingService.getTrackedProjects().get(editor.getProject());

			StructuralCodeContext context = new StructuralCodeContext(projectId);
			context.setProject(editor.getProject().getName());

			Document focusedResource = editor.getDocument();
			PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(editor.getProject());

			VirtualFile file = fileDocumentManager.getFile(editor.getDocument());
			if (file != null) {
				context.setFile(file.getName());

				PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
				populateContext(psiFile, context, offset);
			}

			if (!focusedResource.equals(currentCompilationUnit) || !context.equals(lastCodeContext)) {
				ActivityEvent event = new ActivityEvent(projectId, ActivityType.DocumentFocus, context);
				ActivityState state = ActivityState.createDesignState(projectId);

				recorder.recordState(state);
				recorder.recordEvent(event);

				lastState = state;
				lastEvent = event;
				currentCompilationUnit = focusedResource;
				lastCodeContext = context;
			}
		} catch (Exception e) {
			LogManager.INSTANCE.logError(e, "Problem recording document focus.");
		}
	}

	public synchronized void trackCodingEvent(Editor editor, int offset) {
		if (editor == null)
			return;

		try {
			FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
			TrackingService trackingService = PluginContext.getInstance().getTrackingService();
			ActivityEvent event = null;

			UUID projectId = trackingService.getTrackedProjects().get(editor.getProject());

			Document focusedResource = editor.getDocument();
			PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(editor.getProject());

			if (focusedResource.equals(currentCompilationUnit) && lastEvent != null && lastEvent.getType() != ActivityType.DocumentEdit) {
				//Currently in design mode, so we need to save an editing event

				CodeContext context = new StructuralCodeContext(projectId);
				context.setProject(editor.getProject().getName());

				VirtualFile file = fileDocumentManager.getFile(editor.getDocument());
				if (file != null) {
					PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
					populateContext(psiFile, context, offset);
				}

				event = new ActivityEvent(projectId, ActivityType.DocumentEdit, context);
				ActivityState state = ActivityState.createDesignState(projectId);

				recorder.recordState(state);
				recorder.recordEvent(event);

				lastState = state;
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
	}

	public StateTracker(int idleDetectionPeriod, Duration idleMinInterval) {
		this.idleDetectionPeriod = idleDetectionPeriod;
		this.idleMinInterval = idleMinInterval;
		this.contextCreator = PluginContext.getInstance().getContextCreator();

		recorder = new ActivitiesRecorder(PluginContext.getInstance());
	}

	public void startTracking() {
		documentListener = new CustomDocumentListener();
		caretListener = new CustomCaretListener();
		visibleAreaListener = new CustomVisibleAreaListener();
		editorMouseListener = new CustomEditorMouseListener();

		ApplicationManager.getApplication().invokeLater(() -> {

			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.addDocumentListener(documentListener);

			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.addCaretListener(caretListener);

			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.addEditorMouseListener(editorMouseListener);

/*			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.addVisibleAreaListener(visibleAreaListener);*/
		});

		startIdleDetection();
	}

	public void stopTracking() {
		ApplicationManager.getApplication().invokeLater(() -> {

			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.removeDocumentListener(documentListener);

			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.removeCaretListener(caretListener);

			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.removeEditorMouseListener(editorMouseListener);

/*			EditorFactory
					.getInstance()
					.getEventMulticaster()
					.removeVisibleAreaListener(visibleAreaListener);*/
		});

		stopIdleDetection();
	}

	private void startIdleDetection() {
		if (this.idleDetectionExecutor != null)
			return;

		this.idleDetectionExecutor = Executors.newScheduledThreadPool(1);

		Runnable idlePeriodicTask = new Runnable() {

			@Override
			public void run() {
				try {
					TrackingConsole.getInstance().trackMessage("Idle detection task executed");
					checkIdleStatus();
				} catch (Exception e) {
					TrackingConsole.getInstance().trackMessage("Idle detection task error " + e.getMessage());
				}
			}
		};

		this.idleDetectionExecutor.scheduleAtFixedRate(idlePeriodicTask, idleDetectionPeriod, idleDetectionPeriod, TimeUnit.SECONDS);
	}

	private void stopIdleDetection() {
		if (this.idleDetectionExecutor != null) {
			this.idleDetectionExecutor.shutdownNow();
			this.idleDetectionExecutor = null;
		}
	}

	private void checkIdleStatus() {
		if (lastState.getType() == ActivityType.Idle) {
			// if last event was idle, keep recording idle
			lastState = ActivityState.createIdleState(PluginContext.UNASSIGNED_PROJECT);
			recorder.recordState(lastState);
		}
		else {
			// if last state was not idle check if it is time to go idle
			DateTime now = DateTime.now();
			Duration duration = new Duration(lastEvent.getCreationTime(), now);
			if (duration.compareTo(idleMinInterval) < 0) {
				lastState = ActivityState.createIdleState(PluginContext.UNASSIGNED_PROJECT);
				recorder.recordState(lastState);
			}
		}
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
