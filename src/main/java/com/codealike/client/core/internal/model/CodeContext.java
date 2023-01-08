package com.codealike.client.core.internal.model;

import java.util.UUID;

public interface CodeContext {

    UUID getProjectId();

    String getProject();

    void setProject(String project);

    String getFile();

    void setFile(String file);

    int getLine();

    void setLine(int lineNumber);

    String getPackageName();

    void setPackageName(String packageName);

    String getClassName();

    void setClassName(String className);

    String getMemberName();

    void setMemberName(String memberName);

    boolean isEquivalent(CodeContext context);

}