package com.codealike.client.core.internal.processing;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.codealike.client.core.internal.dto.ActivityEntryInfo;
import com.codealike.client.core.internal.dto.ActivityInfo;
import com.codealike.client.core.internal.dto.CodeContextInfo;
import com.codealike.client.core.internal.dto.ProjectContextInfo;
import com.codealike.client.core.internal.model.ActivityEvent;
import com.codealike.client.core.internal.model.ActivityState;
import com.codealike.client.core.internal.startup.PluginContext;

public class ActivityInfoProcessor {
	private List<ActivityState> processedStates;
	private List<ActivityEvent> processedEvents;
	private DateTime batchStart;
	private DateTime batchEnd;
	
	public ActivityInfoProcessor(List<ActivityState> states, List<ActivityEvent> events, DateTime batchStart, DateTime batchEnd) {
		PluginContext.getInstance().getLogger().log("ActivityInfoProcessor created for [States: " + states.size() + ", Events: " + events.size() + "]");
		this.processedStates = states;
		this.processedEvents = events;
		this.batchStart = batchStart;
		this.batchEnd = batchEnd;
	}

    public List<ActivityInfo> getSerializableEntities(String machineName, String instanceName, String client, String extension)
    {
    	List<ProjectContextInfo> projects = getProjectsInfo(this.processedEvents);
    	List<ActivityInfo> activity = new LinkedList<ActivityInfo>();
    	
    	for (ProjectContextInfo project : projects) {
    		UUID batchId = UUID.randomUUID();
            ActivityInfo activityInfo = new ActivityInfo(instanceName, project.getProjectId(), batchId, batchStart, batchEnd);
            activityInfo.setMachine(machineName);
            activityInfo.setClient(client);
            activityInfo.setExtension(extension);
            List<ProjectContextInfo> projectsOfThisProject = new LinkedList<ProjectContextInfo>();
            projectsOfThisProject.add(project);
            activityInfo.setProjects(projectsOfThisProject);
            activityInfo.setStates(getBatchStates(this.processedStates, project.getProjectId()));
            activityInfo.setEvents(getBatchEvents(this.processedEvents, project.getProjectId()));
            
            activity.add(activityInfo);
    	}
    	
        return activity;
    }

	private List<ActivityEntryInfo> getBatchEvents(List<ActivityEvent> processedEvents, UUID projectId) {
		List<ActivityEntryInfo> batchEvents = new LinkedList<ActivityEntryInfo>();
				
		for (ActivityEvent state : processedEvents) {
			if (state.getProjectId() != projectId) {
				continue;
			}
			ActivityEntryInfo info = new ActivityEntryInfo(state.getProjectId());
			info.setStart(state.getCreationTime());
			info.setEnd(state.getCreationTime().plus(state.getDuration()));
			info.setType(state.getType());
			info.setDuration(state.getDuration());
			CodeContextInfo context = new CodeContextInfo(state.getProjectId());
			context.setNamespace(state.getContext().getPackageName());
			context.setClass(state.getContext().getClassName());
			context.setMember(state.getContext().getMemberName());
			context.setFile(state.getContext().getFile());
			
			info.setContext(context);
		
			batchEvents.add(info);
		}
		
		return batchEvents;
	}

	private List<ActivityEntryInfo> getBatchStates(List<ActivityState> processedStates, UUID projectId) {
		List<ActivityEntryInfo> batchStates = new LinkedList<ActivityEntryInfo>();
		
		for (ActivityState event : processedStates) {
			if (event.getProjectId() != projectId) {
				continue;
			}
			ActivityEntryInfo info = new ActivityEntryInfo(event.getProjectId());
			info.setStart(event.getCreationTime());
			info.setEnd(event.getCreationTime().plus(event.getDuration()));
			info.setType(event.getType());
			info.setDuration(event.getDuration());
		
			batchStates.add(info);
		}
		
		return batchStates;
	}

	private List<ProjectContextInfo> getProjectsInfo(List<ActivityEvent> processedEvents) {
		List<UUID> projectIds = new LinkedList<UUID>();
		List<ProjectContextInfo> projectsInfo = new LinkedList<ProjectContextInfo>();
		
		for (ActivityEvent event : processedEvents) {
			if (!projectIds.contains(event.getProjectId())) {
				projectIds.add(event.getProjectId());
				
				projectsInfo.add(new ProjectContextInfo(event.getProjectId(), event.getContext().getProject()));
			}
		}
		
		if (!projectIds.contains(PluginContext.UNASSIGNED_PROJECT)) {
			projectsInfo.add(new ProjectContextInfo(PluginContext.UNASSIGNED_PROJECT, "Unassigned"));
		}
		
		return projectsInfo;
	}

}
