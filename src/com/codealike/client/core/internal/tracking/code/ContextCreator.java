package com.codealike.client.core.internal.tracking.code;

import java.util.UUID;
/*
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.ui.IEditorPart;
*/
import com.codealike.client.core.internal.model.CodeContext;
import com.codealike.client.core.internal.model.StructuralCodeContext;
import com.codealike.client.core.internal.startup.PluginContext;
import com.intellij.openapi.project.Project;
//import com.codealike.client.core.internal.utils.CodeContextUtils;
//import com.codealike.client.core.internal.utils.EditorUtils;

public class ContextCreator {

	public CodeContext createCodeContextInternal(/*JavaEditor javaEditor,*/ UUID projectId) /*throws JavaModelException*/ {
		/*IJavaElement element = null;
		
		element = EditorUtils.getJavaSelectedElement(javaEditor);
		CodeContext context = CodeContextUtils.createCodeContext(element);
		CodeContextUtils.addFilename(javaEditor, context);
		
		return context;*/
		return null;
	}
	
	/*public CodeContext createCodeContextInternal(IEditorPart editor, UUID projectId) {
		IProject project = PluginContext.getInstance().getTrackingService().getProject(projectId);

		StructuralCodeContext context = new StructuralCodeContext(projectId);
		context.setProject(project.getName());
		//CodeContextUtils.addFilename(editor, context);
		
		return context;
	}
	*//*
	public CodeContext createCodeContext(IEditorPart editor, UUID projectId) throws JavaModelException {
		if (editor instanceof JavaEditor) {
			return createCodeContextInternal((JavaEditor)editor, projectId);
		}
		else {
			return createCodeContextInternal(editor, projectId);
		}
	}
	*/
	public CodeContext createCodeContext(Project project) {
		UUID projectId = PluginContext.getInstance().getTrackingService().getUUID(project);
		
		StructuralCodeContext context = new StructuralCodeContext(projectId);
		context.setProject(project.getName());
		
		return context;
	}

}
