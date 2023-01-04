package com.codealike.client.core.internal.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	
	public void setMember(String memberName) {
		this.member = memberName;
	}
	
	@JsonProperty("class")
	public void setClass(String className) {
		this.className = className;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public void setProjectId(UUID projectId) {
		this.projectId = projectId;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public String getFile() {
		return this.file;
	}
	
	public String getNamespace() {
		return namespace;
	}

	@JsonProperty("class")
	public String getClassName() {
		return className;
	}

	public String getMember() {
		return member;
	}
}
