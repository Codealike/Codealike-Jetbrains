package com.codealike.client.core.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.startup.PluginContext;

public class ActivityState {
	
	public static final ActivityState NONE = new ActivityState();
	
	protected Period duration;
	protected ActivityType type;
	protected DateTime creationTime;
	protected UUID projectId;
	
	public static ActivityState createDebugState(UUID projectId) {
		return new ActivityState(projectId, ActivityType.Debugging, DateTime.now());
	}
	
	public static ActivityState createDesignState(UUID projectId) {
		return new ActivityState(projectId, ActivityType.Coding, DateTime.now());
	}
	
	public static ActivityState createBuildState(UUID projectId) {
		return new ActivityState(projectId, ActivityType.Building, DateTime.now());
	}

	public static ActivityState createSystemState(UUID projectId) {
		return new ActivityState(projectId, ActivityType.System, DateTime.now());
	}
	
	public static IdleActivityState createIdleState(UUID projectId) {
		return IdleActivityState.createNew(projectId);
	}
	
	public static List< ActivityState> createNullState() {
		List<ActivityState> nullStates = new ArrayList<ActivityState>();
		for (UUID projectId : PluginContext.getInstance().getTrackingService().getTrackedProjects().inverse().keySet()) {
			nullStates.add(NullActivityState.createNew(projectId));
		}
		return nullStates;
	}
	
	public static ActivityState createNullState(UUID projectId) {
		return NullActivityState.createNew(projectId);
	}
	
	protected ActivityState()
	{
		this.type = ActivityType.None;
		this.duration = Period.ZERO;
	}
	
	protected ActivityState(UUID projectId, ActivityType type, DateTime creationTime) {
		this.projectId = projectId;
		this.creationTime = creationTime;
		this.type = type;
		this.duration = Period.ZERO;
	}

	public Period getDuration() {
		return duration;
	}

	public void setDuration(Period duration) {
		this.duration = duration;
	}
	
	public ActivityType getType()
	{
		return type;
	}

	public DateTime getCreationTime() {
		return creationTime;
	}

	public ActivityState recreate() {
		return new ActivityState(this.projectId, this.type, DateTime.now());
	}

	public UUID getProjectId() {
		return this.projectId;
	}

	public void setCreationTime(DateTime startWorkspaceDate) {
		this.creationTime = startWorkspaceDate;
	}

	public boolean canExpand() {
		return this.type != ActivityType.System && this.type != ActivityType.Building && 
				this.type != ActivityType.Idle && this.type != ActivityType.Debugging;
	}
	
	public boolean canShrink() {
		return this.type == ActivityType.Debugging || this.type == ActivityType.Coding || 
				this.type == ActivityType.Idle;
	}

}
