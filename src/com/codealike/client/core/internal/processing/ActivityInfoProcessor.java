package com.codealike.client.core.internal.processing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.codealike.client.core.internal.dto.ActivityEntryInfo;
import com.codealike.client.core.internal.dto.ActivityInfo;
import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.dto.CodeContextInfo;
import com.codealike.client.core.internal.dto.ProjectContextInfo;
import com.codealike.client.core.internal.model.ActivityEvent;
import com.codealike.client.core.internal.model.ActivityState;
import com.codealike.client.core.internal.model.BuildActivityEvent;
import com.codealike.client.core.internal.model.NullActivityState;
import com.codealike.client.core.internal.startup.PluginContext;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ActivityInfoProcessor {
	
//	private static final Period THRESHOLD = Period.seconds(7);
	
	private List<ActivityState> processedStates;
	private List<ActivityEvent> processedEvents;
	
	public ActivityInfoProcessor(TreeMap<DateTime, List<ActivityState>> states, TreeMap<DateTime, List<ActivityEvent>> events) {
		this.processedStates = processStates(states);
		this.processedEvents = processEvents(events);
	}
	
	public boolean isValid() {
		return this.processedStates != null && !this.processedStates.isEmpty();
	}

	private List<ActivityEvent> processEvents(TreeMap<DateTime, List<ActivityEvent>> events) {
		List<ActivityEvent> processedEvents = new LinkedList<ActivityEvent>();
		
		if (events == null || events.isEmpty()) {
			return processedEvents;
		}
		
		Iterator<DateTime> it = events.keySet().iterator();
		
		DateTime lastDate = it.next();
		List<ActivityEvent> lastEvents = events.get(lastDate);
		for (ActivityEvent lastEvent : lastEvents) {
			if (lastEvent.getType() != ActivityType.Event) {
       		 processedEvents.add(lastEvent);
			}
		}
		
		for (Iterator<DateTime> iterator = it; iterator.hasNext();) {
			DateTime currentDate = iterator.next();
			List<ActivityEvent> currentEvents = events.get(currentDate);
			
			 for (ActivityEvent lastEvent : lastEvents) {
				 if (!lastEvent.isMarker() && lastEvent.getDuration().equals(Period.ZERO))
				 {
					 lastEvent.setDuration(new Period(lastEvent.getCreationTime(), currentDate));
				 }
			 }
	         
	         for (ActivityEvent currentEvent: currentEvents) {
	        	 if (currentEvent.isBuildEvent()) {
	        		 BuildActivityEvent buildEvent = tryToProcessBuildEvent((BuildActivityEvent) currentEvent);
	        		 if ( buildEvent != null) {
	        			 processedEvents.add(buildEvent);
	        		 }
	        	 }
	        	 else if (currentEvent.getType() != ActivityType.Event) {
	        		 processedEvents.add(currentEvent);
	        	 }
	         }
	         
	         lastEvents = currentEvents;
		}
		
		return processedEvents;
	}
	
	private HashMap<UUID, BuildActivityEvent> buildEvents = new HashMap<UUID, BuildActivityEvent>();

	private BuildActivityEvent tryToProcessBuildEvent(BuildActivityEvent currentEvent) {
		BuildActivityEvent eventToReturn = null;
		if (currentEvent.getType() == ActivityType.BuildProject) {
			buildEvents.put(currentEvent.getProjectId(), currentEvent);
		}
		else if (currentEvent.getType() == ActivityType.BuildProjectSucceeded ||
				 currentEvent.getType() == ActivityType.BuildProjectFailed ||
				 currentEvent.getType() == ActivityType.BuildSolutionCancelled) {
			BuildActivityEvent event = buildEvents.get(currentEvent.getProjectId());
			if (event != null) {
				buildEvents.remove(currentEvent.getProjectId());
				
				currentEvent.setDuration(new Period(event.getCreationTime(), currentEvent.getCreationTime()));
				currentEvent.setCreationTime(event.getCreationTime());
				
				eventToReturn = currentEvent;
			}
		}
		else if (currentEvent.getType() == ActivityType.BuildSolutionSucceded ||
				 currentEvent.getType() == ActivityType.BuildSolutionFailed ||
				 currentEvent.getType() == ActivityType.BuildSolutionCancelled) {
			eventToReturn = currentEvent;
		}
		return eventToReturn;
	}

	private List<ActivityState> processStates(TreeMap<DateTime, List<ActivityState>> states) {
		
		List<ActivityState> processedStates = new LinkedList<ActivityState>();
		
		if (states == null || states.isEmpty()) {
			return processedStates;
		}
		
		Iterator<DateTime> it = states.keySet().iterator();
		
		DateTime lastDate = it.next();
		List<ActivityState> lastStates = states.get(lastDate);
		for (ActivityState lastState : lastStates) {
			if (!(lastState instanceof NullActivityState)) {
				processedStates.add(lastState);
			}
		}
		
		for (Iterator<DateTime> iterator = it; iterator.hasNext();) {
			DateTime currentDate = iterator.next();
			List<ActivityState> currentStates = states.get(currentDate);
			if (currentStates.isEmpty()) {
				continue;
			}
			
			
			//TODO: think what to do with this code.
			//Since we need to take into account the projects for merging states.
			
//			ActivityState lastCurrentState = currentStates.get(currentStates.size()-1);
//			for (ActivityState lastState : lastStates) {
//				if (lastState.canExpand() && lastCurrentState.canShrink()) {
//		        	 DateTime creation = lastCurrentState.getCreationTime();
//		        	 if (lastCurrentState.getDuration().toDurationFrom(creation).isLongerThan(THRESHOLD.toDurationFrom(creation)))
//	                 {
//		        		 lastState.setDuration(new Period(lastState.getCreationTime(), currentDate));
//	                     continue;
//	                 }
//		         }
//			}
			
			for (ActivityState currentState : currentStates) {
				if (!(currentState instanceof NullActivityState)) {
					processedStates.add(currentState);
				}
			}
		}
			
		
		return processedStates;
	}
	
    public List<ActivityInfo> getSerializableEntities(String machineName, String instanceName, String client, String extension)
    {
    	List<ProjectContextInfo> projects = getProjectsInfo(this.processedEvents);
    	List<ActivityInfo> activity = new LinkedList<ActivityInfo>();
    	
    	for (ProjectContextInfo project : projects) {
    		UUID batchId = UUID.randomUUID();
            ActivityInfo activityInfo = new ActivityInfo(instanceName, project.getProjectId(), batchId);
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
    
    //Checks the last registered activity is not only idle
    public boolean isActivityValid(List<ActivityInfo> activity) {
    		Collection<ActivityInfo> filtered = Collections2.filter(activity, new Predicate<ActivityInfo>() {

				@Override
				public boolean apply(ActivityInfo a) {
					for (ActivityEntryInfo state : a.getStates()) {
						return state.getType() != ActivityType.Idle;
					}
					return false;
				}
    		});
			return !filtered.isEmpty();
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
