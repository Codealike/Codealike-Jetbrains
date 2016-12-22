package com.codealike.client.core.internal.utils;

import java.util.UUID;

import com.codealike.client.core.internal.model.CodeContext;
import com.codealike.client.core.internal.model.StructuralCodeContext;
import com.codealike.client.core.internal.model.exception.NonExistingResourceException;
import com.codealike.client.core.internal.startup.PluginContext;

public class CodeContextUtils {
	
	/*public static IType findClass(IJavaElement element) {
		
		if (element.getElementType() == JavaElement.TYPE)
		{
			return (IType)element;
		}
		
		IJavaElement parent = element.getParent();
		if (parent != null)
			return findClass(parent);
		else 
			return null;
	}*/
	
	/*public static String findPackageName(IJavaElement element)
	{
		if (element.getElementType() == JavaElement.PACKAGE_DECLARATION || element.getElementType() == JavaElement.PACKAGE_FRAGMENT)
		{
			return element.getElementName();
		}
		
		IJavaElement parent = element.getParent();
		if (parent != null)
			return findPackageName(parent);
		else 
			return "";
	}*/
	
	
	/*public static String findMemberName(IJavaElement element)
	{
		if (element.getElementType() == JavaElement.FIELD)
		{
			return ((IField) element).getElementName();
		}
		else if (element.getElementType() == JavaElement.METHOD) {
			return ((IMethod) element).getElementName();
		}
		
		IJavaElement parent = element.getParent();
		if (parent != null)
			return findMemberName(parent);
		else 
			return "";
	}*/
	/*
	public static CodeContext createCodeContext(IJavaElement element) {
		
		UUID projectId = PluginContext.getInstance().getTrackingService().getTrackedProjects().get(element.getJavaProject().getProject());
		
		StructuralCodeContext context = new StructuralCodeContext(projectId);
		
		IType classElement = findClass(element);
		String fullClassName =  classElement != null ? classElement.getFullyQualifiedName() : null;
		String className = "";
		if ( fullClassName != null) {
			String[] splittedClassName = fullClassName.split(".");
			className = (splittedClassName.length > 0) ? splittedClassName[splittedClassName.length - 1] : fullClassName;
		}
		context.setClassName(className);
		
		IResource resource = element.getResource();
		if (resource != null)
		{
			context.setFile(resource.getFullPath().toString());
		}
		
		context.setPackageName(findPackageName(element));
		context.setMemberName(findMemberName(element));
		
		context.setProject(((IProject)element.getJavaProject().getProject()).getName());
		
		return context;
	}
	*/
	/*public static void addFilename(IEditorPart editor, CodeContext context) {
		try {
			IResource resource = EditorUtils.getActiveResource(editor);
			if (resource != null) {
				String fileName = String.format("%s", resource.getName());
				context.setFile(fileName);
			}
		}
		catch (NonExistingResourceException e) {
			LogManager.INSTANCE.logError(e, "Trying to find unexisting resource.");
		}
	}
*/
}
