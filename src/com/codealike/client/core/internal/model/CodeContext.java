package com.codealike.client.core.internal.model;

import java.util.UUID;

public interface CodeContext {

	public UUID getProjectId();

	public String getProject();

	public void setProject(String project);

	public String getFile();

	public void setFile(String file);

	public int getLine();

	public void setLine(int lineNumber);

	public String getPackageName();

	public void setPackageName(String packageName);

	public String getClassName();

	public void setClassName(String className);

	public String getMemberName();

	public void setMemberName(String memberName);

}