package com.codealike.client.core.internal.model;

import java.util.UUID;

import com.codealike.client.core.internal.startup.PluginContext;

public class StructuralCodeContext implements CodeContext {

	private UUID projectId;
	private String project;
	private String file;
	private String packageName;
	private String className;
	private String memberName;
	
	public static StructuralCodeContext createNullContext() {
		return new StructuralCodeContext(PluginContext.UNASSIGNED_PROJECT, "", "", "", "", "");
	}
	
	public StructuralCodeContext(UUID projectId)
	{
		this(projectId, "", "", "", "", "");
	}
	
	protected StructuralCodeContext(UUID projectId, String project, String file, String packageName, String className, String memberName) {
		this.projectId = projectId;
		this.project = project;
		this.file = file;
		this.packageName = packageName;
		this.className = className;
		this.memberName = memberName;
	}
	
	@SuppressWarnings("unused")
	private StructuralCodeContext() { }
	
	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#getProjectId()
	 */
	@Override
	public UUID getProjectId()
	{
		return projectId;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#getProject()
	 */
	@Override
	public String getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#setProject(java.lang.String)
	 */
	@Override
	public void setProject(String project) {
		this.project = project;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#getFile()
	 */
	@Override
	public String getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#setFile(java.lang.String)
	 */
	@Override
	public void setFile(String file) {
		this.file = file;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#getPackageName()
	 */
	@Override
	public String getPackageName() {
		return packageName;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#setPackageName(java.lang.String)
	 */
	@Override
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#getClassName()
	 */
	@Override
	public String getClassName() {
		return className;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#setClassName(java.lang.String)
	 */
	@Override
	public void setClassName(String className) {
		this.className = className;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#getMemberName()
	 */
	@Override
	public String getMemberName() {
		return memberName;
	}

	/* (non-Javadoc)
	 * @see com.codealike.client.eclipse.internal.model.CodeContext#setMemberName(java.lang.String)
	 */
	@Override
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	};
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		if (project != null && !project.isEmpty()) {
			buffer.append("ProjectModel: "+this.project+"\n");
		}
		if (packageName != null && !packageName.isEmpty()) {
			buffer.append("Package: "+this.packageName+"\n");
		}
		if (className != null && !className.isEmpty()) {
			buffer.append("Class: "+this.className+"\n");
		}
		
		if (memberName != null && !memberName.isEmpty()) {
			buffer.append("Member: "+this.memberName+"\n");
		}
		
		if (file != null && !file.isEmpty()) {
			buffer.append("File: "+this.file+"\n");
		}
		
		return buffer.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result
				+ ((memberName == null) ? 0 : memberName.hashCode());
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result
				+ ((projectId == null) ? 0 : projectId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StructuralCodeContext other = (StructuralCodeContext) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (projectId == null) {
			if (other.projectId != null)
				return false;
		} else if (!projectId.equals(other.projectId))
			return false;
		return true;
	}
}
