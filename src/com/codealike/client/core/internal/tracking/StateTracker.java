package com.codealike.client.core.internal.tracking;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.codealike.client.core.internal.model.*;
import com.codealike.client.core.internal.utils.TrackingConsole;
import com.codealike.client.intellij.EventListeners.CustomCaretListener;
import com.codealike.client.intellij.EventListeners.CustomDocumentListener;
import com.codealike.client.intellij.EventListeners.CustomEditorMouseListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.joda.time.DateTime;
import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.services.TrackingService;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.tracking.ActivitiesRecorder.FlushResult;
import com.codealike.client.core.internal.tracking.code.ContextCreator;
import org.joda.time.Period;

public class StateTracker {

	private ActivitiesRecorder recorder;
	private ActivityState lastState;

	private ActivityEvent lastEvent;
	private ContextCreator contextCreator;
	private ScheduledExecutorService idleDetectionExecutor;

	private DocumentListener documentListener;
	private CaretListener caretListener;
	private CustomEditorMouseListener editorMouseListener;

	private synchronized void populateContext(PsiFile file, CodeContext context, int offset, int line) {
		try {
			// sets file name
			context.setFile(file.getName());
			context.setLine(line);

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
			PluginContext.getInstance().getLogger().logInfo(String.format("Could not track activity in file %s.", context.getFile()));
		}
	}

	private synchronized StructuralCodeContext gatherEventContextInformation(UUID projectId,
																			 Editor editor,
																			 int offset,
																			 int line) {
		StructuralCodeContext context = null;

		// try get information about the file
		VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());

		// if no file was obtained or file is special ide file 'fragment.java' skip process
		if (file == null || file.getName() == "fragment.java")
			return null;

		// create code context and populate with event information
		context = new StructuralCodeContext(projectId);
		context.setProject(editor.getProject().getName());

		PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(editor.getProject());
		PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
		populateContext(psiFile, context, offset, line);

		return context;
	}

	public  void trackDocumentFocus(Editor editor, int offset, int line) {
		if (editor == null)
			return;

		try {
			// obtain project id
			UUID projectId = TrackingService.getInstance().getTrackedProjects().get(editor.getProject());

			// obtain event context
			StructuralCodeContext context = gatherEventContextInformation(projectId, editor, offset, line);
			if (context == null)
				return;

			// create related events
			ActivityEvent event = new ActivityEvent(projectId, ActivityType.DocumentFocus, context);
			ActivityState state = ActivityState.createDesignState(projectId);

			if (lastEvent.getType() == ActivityType.DocumentEdit &&
					lastEvent.getContext().isEquivalent(event.getContext())) {
				// this fix the issue with focus event comming right after
				// each coding event.
				PluginContext.getInstance().getLogger().logInfo("Focus event skiped");
				return;
			}

			PluginContext.getInstance().getLogger().logInfo("Document focus tracked");

			// record events to be processed
			recorder.recordState(state);
			recorder.recordEvent(event);

			// remember last state and event for next loop
			lastState = state;
			lastEvent = event;

		} catch (Exception e) {
			PluginContext.getInstance().getLogger().logError(e, "Problem recording document focus.");
		}
	}

	public synchronized void trackCodingEvent(Editor editor, int offset, int line) {
		if (editor == null)
			return;

		try {
			// obtain project id
			UUID projectId = TrackingService.getInstance().getTrackedProjects().get(editor.getProject());

			// obtain event context
			StructuralCodeContext context = gatherEventContextInformation(projectId, editor, offset, line);
			if (context == null)
				return;

			// create related events
			ActivityEvent event = new ActivityEvent(projectId, ActivityType.DocumentEdit, context);
			ActivityState state = ActivityState.createDesignState(projectId);

			PluginContext.getInstance().getLogger().logInfo("Coding event tracked");

			// record events to be processed
			recorder.recordState(state);
			recorder.recordEvent(event);

			// remember last state and event for next loop
			lastState = state;
			lastEvent = event;

		} catch (Exception e) {
			PluginContext.getInstance().getLogger().logError(e, "Problem recording document edit.");
		}
	}

	public void startTrackingProject(Project project, UUID projectId, DateTime startWorkspaceDate) {
		ActivityEvent openSolutionEvent = new ActivityEvent(projectId, ActivityType.OpenSolution, contextCreator.createCodeContext(project));
		openSolutionEvent.setCreationTime(startWorkspaceDate);

		ActivityState systemState = ActivityState.createSystemState(projectId);
		systemState.setCreationTime(startWorkspaceDate);

		lastState = recorder.recordState(systemState);
		lastEvent = recorder.recordEvent(openSolutionEvent);
		lastState = recorder.recordState(ActivityState.createIdleState(projectId));
	}

	public StateTracker() {
		contextCreator = PluginContext.getInstance().getContextCreator();
		recorder = new ActivitiesRecorder(PluginContext.getInstance());
	}

	public void startTracking() {
		documentListener = new CustomDocumentListener();
		caretListener = new CustomCaretListener();
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
		});

		startIdleDetection();
	}

	public void stopTracking() {
		ApplicationManager.getApplication().invokeLater(() -> {

			if (documentListener != null) {
				EditorFactory
						.getInstance()
						.getEventMulticaster()
						.removeDocumentListener(documentListener);
			}

			if (caretListener != null) {
				EditorFactory
						.getInstance()
						.getEventMulticaster()
						.removeCaretListener(caretListener);
			}

			if (editorMouseListener != null) {
				EditorFactory
						.getInstance()
						.getEventMulticaster()
						.removeEditorMouseListener(editorMouseListener);
			}
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

		int idleDetectionPeriod = PluginContext.getInstance().getConfiguration().getIdleCheckInterval();
		this.idleDetectionExecutor.scheduleAtFixedRate(idlePeriodicTask, idleDetectionPeriod, idleDetectionPeriod, TimeUnit.MILLISECONDS);
	}

	private void stopIdleDetection() {
		if (this.idleDetectionExecutor != null) {
			this.idleDetectionExecutor.shutdownNow();
			this.idleDetectionExecutor = null;
		}
	}

	private void checkIdleStatus() {
		// if last state was idle, it seems to be still idle
		if (recorder.getLastState().getType() == ActivityType.Idle) {
			recorder.updateLastState();
		}
		else {
			DateTime currentTime = DateTime.now();
			long idleMaxPeriodInSeconds = PluginContext.getInstance().getConfiguration().getIdleMinInterval() / 1000;
			long elapsedFromLastEventInSeconds = new Period(recorder.getLastEventTime(), currentTime).toStandardSeconds().getSeconds();
			if (elapsedFromLastEventInSeconds >= idleMaxPeriodInSeconds) {
				// not needed because idea cannot track another type than coding
				// save last state type before going iddle
				//Codealike.stateBeforeIdle = recorder.lastState.type;

				// record idle state
				recorder.recordState(ActivityState.createIdleState(PluginContext.UNASSIGNED_PROJECT));
			}
		}
	}

	public FlushResult flush(String identity, String token) {
		try {
			return this.recorder.flush(identity, token);
		}
		catch (Exception e) {
			PluginContext.getInstance().getLogger().logError(e, "Couldn't send data to the server.");
			return FlushResult.Report;
		}
	}

}
