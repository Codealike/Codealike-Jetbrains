/*
 * Copyright (c) 2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.internal.model;

import java.util.UUID;

/**
 * Code context model interface.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public interface CodeContext {

    public UUID getProjectId();

    public String getProject();

    public void setProject(String project);

    public String getFile();

    public void setFile(String file);

    public String getPackageName();

    public void setPackageName(String packageName);

    public String getClassName();

    public void setClassName(String className);

    public String getMemberName();

    public void setMemberName(String memberName);

}