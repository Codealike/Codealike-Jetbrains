/*package com.codealike.client.core.internal.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.internal.editors.text.NonExistingFileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.codealike.client.eclipse.internal.model.exception.NonExistingResourceException;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

@SuppressWarnings("restriction")
public class EditorUtils {
	
	public static IJavaElement getJavaSelectedElement(JavaEditor editor) throws JavaModelException
	{
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();

		ICompilationUnit focusedUnit = (ICompilationUnit) EditorUtility.getEditorInputJavaElement(editor, false);
		IJavaElement element = null;		
		element = focusedUnit.getElementAt(selection.getOffset());
		
		if (element == null)
		{
			element = focusedUnit;
		}
		
		return element;
	}
	
	public static IResource getActiveResource(IEditorPart editor) throws NonExistingResourceException {
		  IEditorInput input = editor.getEditorInput();
		  if (input instanceof NonExistingFileEditorInput) {
			  throw new NonExistingResourceException();
		  }
		  else if (!(input instanceof IFileEditorInput))
	         return null;
	      return ((IFileEditorInput)input).getFile();
	}
	
	public static IProject getActiveProject(IEditorPart editor) throws NonExistingResourceException {
		IResource activeResource = getActiveResource(editor);
		if (activeResource != null) {
			return activeResource.getProject();
		}
		
		return null;
	}
	
	public static IDocument getActiveDocument(IEditorPart editor) {
		if (editor instanceof ITextEditor) {
			return getDocumentFromTextEditor(editor);
		}
		else if (editor instanceof MultiPageEditorPart) {
			MultiPageEditorPart multipageEditor = (MultiPageEditorPart)editor;
			if (multipageEditor.getSelectedPage() instanceof ITextEditor) {
				return getDocumentFromTextEditor((ITextEditor)multipageEditor.getSelectedPage());
			}
		}
		return null;
	}

	private static IDocument getDocumentFromTextEditor(IEditorPart editor) {
		ITextEditor textEditor = (ITextEditor)editor;
		return textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
	}
	

	public static List<IMarker> getCompilationErrors(IProject project) throws CoreException {
		if (project == null) {
			throw new IllegalArgumentException("Project is null.");
		}
		
		IEditorPart activeEditor = WorkbenchUtils.getActiveEditor();
		Predicate<IMarker> errorsFilter = new Predicate<IMarker>() {
			
			@Override
			public boolean apply(IMarker marker) {
				try {
					return ((Integer)marker.getAttribute(IMarker.SEVERITY)) == IMarker.SEVERITY_ERROR;
				} catch (CoreException e) {
					// swallow Exception
					LogManager.INSTANCE.logWarn("Trying to parse build results, but an error occurred");
					return false;
				}
			}
		};
		try {
			if (activeEditor != null && EditorUtils.getActiveProject(activeEditor) == project) {

				IResource resource = getActiveResource(activeEditor);
				if (resource != null) {
					
					List<IMarker> markersList = Arrays.asList(resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE));
					return new ArrayList<IMarker>(Collections2.filter(markersList, errorsFilter));
				}
			}
		} catch (NonExistingResourceException e) {
			LogManager.INSTANCE.logError(e, "Trying to find unexisting resource.");
		}
		
		List<IMarker> projectMarkersList = Arrays.asList(project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE));
		return new ArrayList<IMarker>(Collections2.filter(projectMarkersList, errorsFilter));
	}

}
*/