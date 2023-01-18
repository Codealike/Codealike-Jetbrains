/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import java.util.UUID;

/**
 * Code context model interface.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
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
