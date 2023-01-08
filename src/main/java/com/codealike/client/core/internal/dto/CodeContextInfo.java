package com.codealike.client.core.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CodeContextInfo {

    private String member;
    private String className;
    private String namespace;
    private UUID projectId;
    private String file;

    public CodeContextInfo() {
    }

    public CodeContextInfo(UUID projectId) {
        this.projectId = projectId;
    }

    @JsonProperty("class")
    public void setClass(String className) {
        this.className = className;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @JsonProperty("class")
    public String getClassName() {
        return className;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String memberName) {
        this.member = memberName;
    }
}
