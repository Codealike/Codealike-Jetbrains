package com.codealike.client.core.internal.model;

import java.util.UUID;

import org.joda.time.DateTime;

import com.codealike.client.core.internal.dto.ActivityType;

public class IdleActivityState extends ActivityState {
	
	private DateTime lastActivity;
	
	
	protected static IdleActivityState createNew(UUID projectId) {
		IdleActivityState state = new IdleActivityState(projectId, ActivityType.Idle, DateTime.now());
		state.lastActivity = state.getCreationTime();
		
		return state;
	}

	public IdleActivityState(UUID projectId, ActivityType type, DateTime creationTime)
	{
		super(projectId, type, creationTime);
	}

	public DateTime getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(DateTime lastActivity) {
		this.lastActivity = lastActivity;
	}
	
	@Override
	public IdleActivityState recreate() {
		return new IdleActivityState(this.projectId, this.type, DateTime.now());
	}
	
}
