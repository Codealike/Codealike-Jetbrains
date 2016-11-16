package com.codealike.client.core.internal.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.codealike.client.core.internal.utils.TrackingConsole;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class TrackedProjectManager {
	
	private List<String> trackedProjectsLabels;
	private BiMap<IProject, UUID> trackedProjects;
	
	public TrackedProjectManager() {
		this.trackedProjects = HashBiMap.create();
		this.trackedProjectsLabels = new ArrayList<String>();
		
	}
	
	public List<String> getTrackedProjectsLabels() {
		return trackedProjectsLabels;
	}
	
	public void setTrackedProjectsLabels(List<String> trackedProjectsLabels) {
		this.trackedProjectsLabels = trackedProjectsLabels;
		firePropertyChange("trackedProjectsLabels", null, null);
	}
	
	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public boolean trackProject(IProject project, UUID projectId) {
		if (trackedProjects.containsKey(project) || trackedProjects.containsValue(projectId)) {
			return false;
		}
		this.trackedProjects.put(project, projectId);
		this.trackedProjectsLabels.add(String.format("%s - %s", project.getName(), projectId));
		
		TrackingConsole.getInstance().trackProjectStart(project.getName(), projectId);
		
		firePropertyChange("trackedProjectsLabels", null, null);
		return true;
	}

	public UUID getTrackedProjectId(IProject project) {
		return this.trackedProjects.get(project);
	}

	public IProject getTrackedProject(UUID projectId) {
		return this.trackedProjects.inverse().get(projectId);
	}

	public BiMap<IProject, UUID> getTrackedProjects() {
		return this.trackedProjects;
	}

	public boolean isTracked(IProject project) {
		return project != null && this.trackedProjects.get(project) != null;
	}

	public void stopTrackingProject(IProject project) {
		if (!this.trackedProjects.containsKey(project)) {
			return;
		}
		UUID trackedProjectId = getTrackedProjectId(project);
	
		TrackingConsole.getInstance().trackProjectEnd(project.getName(), trackedProjectId);
		this.trackedProjects.remove(project);

		this.trackedProjectsLabels.remove(String.format("%s - %s", project.getName(), trackedProjectId));
		firePropertyChange("trackedProjectsLabels", null, null);
	}
	
	public void stopTracking() {
		Set<IProject> projects = new HashSet<IProject>(trackedProjects.keySet());
		for (IProject trackedProject : projects) {
			stopTrackingProject(trackedProject);
		}
	}

}
