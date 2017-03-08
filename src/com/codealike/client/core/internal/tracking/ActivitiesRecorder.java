package com.codealike.client.core.internal.tracking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Period;

import com.codealike.client.core.api.ApiClient;
import com.codealike.client.core.api.ApiResponse;
import com.codealike.client.core.api.ApiResponse.Status;
import com.codealike.client.core.internal.dto.ActivityInfo;
import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.model.ActivityEvent;
import com.codealike.client.core.internal.model.ActivityState;
import com.codealike.client.core.internal.model.NullActivityState;
import com.codealike.client.core.internal.model.StructuralCodeContext;
import com.codealike.client.core.internal.processing.ActivityInfoProcessor;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.utils.GenericExtensionFilter;
import com.codealike.client.core.internal.utils.LogManager;
import com.codealike.client.core.internal.utils.TrackingConsole;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ActivitiesRecorder {

	private TreeMap<DateTime, List<ActivityState>> states;
	private Map<UUID, List<ActivityEvent>> events;
	private ActivityEvent lastEvent;
	private DateTime lastStateDate;
	private PluginContext context;
	private ActivityState lastRecordedState;
	
	public ActivitiesRecorder(PluginContext context)
	{
		this.states = new TreeMap<DateTime, List<ActivityState>>(DateTimeComparator.getInstance());
		this.events = new HashMap<UUID, List<ActivityEvent>>();
		this.context = context;
	}
	
	public synchronized ActivityState recordState(ActivityState state) {
		ActivityState lastStateOfAllStates = null;
		List<ActivityState> lastStates = null;
		DateTime currentDate = state.getCreationTime();

		if (lastRecordedState != null
				&& state.getType() == lastRecordedState.getType()
				&& state.getProjectId() == lastRecordedState.getProjectId()) {
			return lastRecordedState;
		}

		if (lastStateDate != null && !lastStateDate.equals(currentDate)) {
			lastStates = states.get(lastStateDate);
		}
		
		if (states.get(currentDate)==null) {
			states.put(currentDate, new LinkedList<ActivityState>());
			lastStateDate = currentDate;
		}
		states.get(currentDate).add(state);
		
		if (lastStates != null && !lastStates.isEmpty()) {
			lastStateOfAllStates = lastStates.get(lastStates.size()-1);
			
			for(ActivityState lastState : lastStates) {
				if (lastState != null && lastState.getDuration().equals(Period.ZERO)) {
					lastState.setDuration(new Period(lastState.getCreationTime(), currentDate));
					
					if (!(lastState instanceof NullActivityState)) {
						TrackingConsole.getInstance().trackState(lastState);
					}
				}
			}
		}
		
		// If the current event cannot span multiple states then we set its duration to finish now.
		if (lastStateDate != null && lastEvent != null && !lastEvent.canSpan()) {
			// Duration = State.EndTime - Event.StartTime;
            lastEvent.setDuration(new Period(lastEvent.getCreationTime(), currentDate));
            
            if (lastEvent.getType() != ActivityType.Event) {
            	recordEvent(new ActivityEvent(lastEvent.getProjectId(), ActivityType.Event, StructuralCodeContext.createNullContext()));
            }
		}

		// keep track of last tracked state
		lastRecordedState = lastStateOfAllStates;

		return lastStateOfAllStates;
	}

	public ActivityState getLastState() {
		return lastRecordedState;
	}

	private ActivityEvent getLastEvent(UUID projectId) {
		ActivityEvent lastEvent = null;
		List<ActivityEvent> projectEvents = getProjectEvents(projectId);
		
		if (projectEvents!= null && !projectEvents.isEmpty()) {
			lastEvent = projectEvents.get(projectEvents.size()-1);
		}
		
		return lastEvent;
	}

	public synchronized ActivityState recordStates(List<? extends ActivityState> states) {
		ActivityState lastState = null;
		for (ActivityState activityState : states) {
			lastState = recordState(activityState);
		}
		return lastState;
	}
	
	private List<ActivityEvent> getProjectEvents(UUID projectId) {
		if (events.get(projectId) == null) {
			events.put(projectId, new LinkedList<ActivityEvent>());
		}
		return events.get(projectId);
	}

	public synchronized ActivityEvent recordEvent(ActivityEvent event) {
		ActivityEvent lastEventForThisProject = null;
		List<ActivityEvent> projectEvents = getProjectEvents(event.getProjectId());
		
		lastEventForThisProject = getLastEvent(event.getProjectId());
		
 		projectEvents.add(event);
		
		TrackingConsole.getInstance().trackEvent(event);
		
		lastEvent = event;
		
		return lastEventForThisProject;
	}
	
	public FlushResult flush(String username, String token) throws UnknownHostException {
		TreeMap<DateTime, List<ActivityState>> statesToSend = null;
		TreeMap<DateTime, List<ActivityEvent>> eventsToSend = null;
		
		ActivityState lastState;
		synchronized(this)
		{
			lastState = this.recordStates(ActivityState.createNullState());
			
			statesToSend = this.states;
			eventsToSend = flattenEvents(this.events);
			
			this.states = new TreeMap<DateTime, List<ActivityState>>(DateTimeComparator.getInstance());
			this.events = new HashMap<UUID, List<ActivityEvent>>();
		}
		
		if (lastState != null && !(lastState instanceof NullActivityState)) {
			this.recordState(lastState.recreate());
		}
		
		ActivityInfoProcessor processor = new ActivityInfoProcessor(statesToSend, eventsToSend);
		
		if (!processor.isValid()) {
			return FlushResult.Skip;
		}
		
		String machineName = findLocalHostNameOr("unknown");
		List<ActivityInfo> activityInfoList = processor.getSerializableEntities(machineName, 
				context.getInstanceValue(), context.getIdeName(), context.getPluginVersion());
		String activityLogExtension = context.getProperty("activity-log.extension");
		if (!processor.isActivityValid(activityInfoList)) {
			return FlushResult.Skip;
		}
		FlushResult result = FlushResult.Succeded;
		for(ActivityInfo info : activityInfoList) {
			if (!info.isValid()) {
				continue;
			}
			File cacheFolder = context.getTrackerFolder();
			if (cacheFolder == null) {
				LogManager.INSTANCE.logError("Could not access cache folder. It might not be created.");
				continue;
			}
			FlushResult intermediateResult = trySendEntries(info, username, token);
			if (intermediateResult == FlushResult.Succeded) {
				for (final File fileEntry : cacheFolder.listFiles(new GenericExtensionFilter(activityLogExtension))) {
					trySendEntriesOnFile(fileEntry, username, token);
			    }
				
				if (Boolean.parseBoolean(context.getProperty("activity-log.trace-sent"))) {
					String filename = String.format("%s\\%s%s", cacheFolder.getAbsolutePath(), info.getBatchId(), ".sent");
					
					FileOutputStream stream = null;
					try {
						stream = new FileOutputStream(new File(filename));
						ObjectWriter writer = context.getJsonWriter();
						String json = writer.writeValueAsString(info);
						stream.write(json.getBytes(Charset.forName("UTF-8")));
					}
					catch (Exception e) {
						LogManager.INSTANCE.logError(e, "There was a problem trying to store activity data locally.");
					}
					finally {
						if (stream != null)
						{
							try {
								stream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			else {
				String filename = String.format("%s\\%s%s", cacheFolder.getAbsolutePath(), info.getBatchId(), activityLogExtension);
				
				FileOutputStream stream = null;
				try {
					stream = new FileOutputStream(new File(filename));
					ObjectWriter writer = context.getJsonWriter();
					String json = writer.writeValueAsString(info);
					stream.write(json.getBytes(Charset.forName("UTF-8")));
				}
				catch (Exception e) {
					LogManager.INSTANCE.logError(e, "There was a problem trying to store activity data locally.");
				}
				finally {
					if (stream != null)
					{
						try {
							stream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			if (intermediateResult != FlushResult.Succeded && intermediateResult != FlushResult.Skip)
			{
				result = intermediateResult;
			}
		}
		return result;
	}
	
	private String findLocalHostNameOr(String defaultName) {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) { //see: http://stackoverflow.com/a/40702767/1117552
			return defaultName;
		}
	}

	private void trySendEntriesOnFile(File fileEntry, String username, String token) {
		try {
			FlushResult result = FlushResult.Skip;
			try {
				ActivityInfo activityInfo = context.getJsonMapper().readValue(new FileInputStream(fileEntry), ActivityInfo.class);
				ApiClient client = ApiClient.tryCreateNew(username, token);
				
				ApiResponse<Void> response = client.postActivityInfo(activityInfo);
				if (response.success()) {
					result = FlushResult.Succeded;
				}
				
				if (response.conflict() || response.getStatus() == Status.BadRequest || response.error() || response.notFound()) {
					result = FlushResult.Report;
				}
			} catch (IOException e) {
				LogManager.INSTANCE.logError(e, "There was a problem trying to send offline activity data to the server.");
				result = FlushResult.Report;
			} 
			catch (KeyManagementException e) {
				LogManager.INSTANCE.logError(e, "Could not send data to remote server. There was a problem with SSL configuration.");
				result = FlushResult.Skip;
			}
			
			finally {
				switch (result) {
					case Succeded:
					{
						fileEntry.renameTo(new File(fileEntry.getAbsolutePath().replaceFirst("[.][^.]+$", ".sent")));
					}
					case Report:
					{
						fileEntry.renameTo(new File(fileEntry.getAbsolutePath().replaceFirst("[.][^.]+$", ".error")));
						break;
					}
					case Skip: {
						break;
					}
					case Offline: {
						break;
					}
				default:
					break;
				}
			}
		}
		catch (Throwable t) {
			LogManager.INSTANCE.logError(t, "There was a problem trying to send offline activity data to the server.");
		}
	}

	private FlushResult trySendEntries(ActivityInfo info, String username, String token) {
		try {
			ApiClient client;
			try {
				client = ApiClient.tryCreateNew(username, token);
			}
			catch (KeyManagementException e) {
				LogManager.INSTANCE.logError(e, "Could send activity to remote server. There was a problem with SSL configuration.");
				return FlushResult.Offline;
			}
			
			ApiResponse<Void> response = client.postActivityInfo(info);
			if (!response.success()) {
				LogManager.INSTANCE.logWarn(String.format("There was a problem trying to send activity data to the server (Status: %s). "
						+ "Data will be stored offline until it can be sent.", response.getStatus().toString()));
				if (response.conflict() || response.getStatus() == Status.BadRequest || response.error() || response.notFound()) {
					return FlushResult.Report;
				}
				else 
					return FlushResult.Offline;
			}
			return FlushResult.Succeded;
		}
		catch (Throwable t) {
			LogManager.INSTANCE.logError(t, "There was a problem trying to send activity data to the server.");
			return FlushResult.Report;
		}
	}

	private TreeMap<DateTime, List<ActivityEvent>> flattenEvents(Map<UUID, List<ActivityEvent>> events) {
		TreeMap<DateTime, List<ActivityEvent>>  eventsAsMap = new TreeMap<DateTime, List<ActivityEvent>>(DateTimeComparator.getInstance());
		List<ActivityEvent> flatActivityEvents = new ArrayList<ActivityEvent>();
		
		for(UUID project : events.keySet()) {
			List<ActivityEvent> eventsForProject = events.get(project);
			flatActivityEvents.addAll(eventsForProject);
		}
		
		for(ActivityEvent event: flatActivityEvents) {
			DateTime date = event.getCreationTime();
			if (eventsAsMap.get(date)==null) {
				eventsAsMap.put(date, new LinkedList<ActivityEvent>());
			}
			eventsAsMap.get(date).add(event);
		}
		
		return eventsAsMap;
	}
	
	public enum FlushResult {
		Offline,
		Succeded,
		Report,
		Skip
	}
}
