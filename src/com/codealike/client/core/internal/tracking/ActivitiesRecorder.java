package com.codealike.client.core.internal.tracking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.util.LinkedList;
import java.util.List;

import com.codealike.client.core.internal.model.*;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.codealike.client.core.api.ApiClient;
import com.codealike.client.core.api.ApiResponse;
import com.codealike.client.core.api.ApiResponse.Status;
import com.codealike.client.core.internal.dto.ActivityInfo;
import com.codealike.client.core.internal.dto.ActivityType;
import com.codealike.client.core.internal.processing.ActivityInfoProcessor;
import com.codealike.client.core.internal.startup.PluginContext;
import com.codealike.client.core.internal.utils.TrackingConsole;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ActivitiesRecorder {
	private List<ActivityState> states;
	private List<ActivityEvent> events;

	private ActivityEvent lastEvent;
	private ActivityState lastState;

	private PluginContext context;

	private DateTime currentBatchStart;
	private DateTime lastEventTime;
	
	public ActivitiesRecorder(PluginContext context)
	{
		this.states = new LinkedList<>();
		this.events = new LinkedList<>();
		this.context = context;
		this.currentBatchStart = DateTime.now();
	}

	public DateTime getLastEventTime() {
		return lastEventTime;
	}

	public ActivityState getLastState() {
		return lastState;
	}

	public ActivityEvent getLastEvent() {
		return lastEvent;
	}

	/*
	 *  isLastEventPropagating:
	 *  This method checks if provided event is continuation
	 *  of the last event recorded by the system.
	 */
	public boolean isLastEventPropagating(ActivityEvent event) {
		return (lastEvent != null &&
				event.getType() == lastEvent.getType() &&
				event.getContext().getFile() == lastEvent.getContext().getFile() &&
				event.getContext().getLine() == lastEvent.getContext().getLine());
	}

	/*
	 *  isLastStatePropagating:
	 *  This method checks if provided state is continuation
	 *  of the last state recorded by the system
	 */
	public boolean isLastStatePropagating(ActivityState state) {
		return (lastState != null &&
				state.getType() == lastState.getType());
	}

	/*
     *  updateEndableEntityAsOfNowIfRequired:
     *  This method checks if last event/state should be provided
     *  with some spare time given a change. We expect an event
     *  to be a continuous stream of items, if possible without
     *  blank periods of time in between.
     */
	public void updateEndableEntityAsOfNowIfRequired(IEndable endableEntity) {
		// if entity is null, nothing to do here
		if (endableEntity == null)
			return;

		// get idle max interval in seconds
		int idleMinIntervalInSeconds = PluginContext.getInstance().getConfiguration().getIdleCheckInterval() / 1000;

		DateTime currentTime = DateTime.now();
		DateTime entityBaseEnd = endableEntity.getCreationTime().plus(endableEntity.getDuration());
		int elapsedPeriodBetweenLastEventAndNow = new Period(entityBaseEnd, currentTime).toStandardSeconds().getSeconds();

		// if time elapsed between last event activity and now is less than
		// the time it takes to infer user was idle, we track the time as it is
		// else, something happened and idle check was not doing it work, so
		// we consider the duration to be as much as a complete idle period
		if (elapsedPeriodBetweenLastEventAndNow <= idleMinIntervalInSeconds ) {
			endableEntity.setDuration(new Period(endableEntity.getCreationTime(), currentTime));
		}
		else {
			// if event/state type is system related we track
			// whatever it is (no exceptions or checks about duration)
			if (endableEntity.getType() == ActivityType.System
					|| endableEntity.getType() == ActivityType.OpenSolution) {
				endableEntity.setDuration(new Period(endableEntity.getCreationTime(), currentTime));
			}
			else {
				// else, we ensure it does not have inconsistent time
				endableEntity.setDuration(new Period(endableEntity.getCreationTime(), entityBaseEnd.plusSeconds(idleMinIntervalInSeconds).toDateTime()));
			}

		}
	}

	public void updateLastEvent(ActivityEvent event) {
		if (this.lastEvent != null) {
			// when updating an event, we also
			// update context information as
			// the last event should be the most complete
			this.lastEvent.setContext(event.getContext());

			// also update last finishing time as of now
			this.updateEndableEntityAsOfNowIfRequired(this.lastEvent);
		}
	}

	public void updateLastState() {
		// if there is a last state
		// update it's duration as of now
		if (this.lastState != null) {
			this.updateEndableEntityAsOfNowIfRequired(this.lastState);
		}
	}

	public synchronized ActivityState recordState(ActivityState state) {

		if (this.isLastStatePropagating(state)) {
			this.updateLastState();
		}
		else {
			// set the finalization of the last state
			this.updateEndableEntityAsOfNowIfRequired(this.lastState);

			// if state changed, last event is finished for sure
			this.updateEndableEntityAsOfNowIfRequired(this.lastEvent);

			// adds the state to the current session
			this.states.add(state);

			// sets state as last state
			this.lastState = state;
		}

		TrackingConsole.getInstance().trackState(lastState);

		// and return
		return lastState;
	}

	public synchronized ActivityEvent recordEvent(ActivityEvent event) {

		if (this.isLastEventPropagating(event)) {
			this.updateLastEvent(event);
		}
		else {
			// set the finalization of the last event
			this.updateEndableEntityAsOfNowIfRequired(this.lastEvent);

			// adds the event to the current session
			this.events.add(event);

			// sets event as last event
			this.lastEvent = event;
		}

		// saves time from last event
		this.lastEventTime = DateTime.now();

		TrackingConsole.getInstance().trackEvent(this.lastEvent);

		// and return
		return lastEvent;
	}

	private Boolean HasOnlyIdleState() {
		for (ActivityState state:this.states) {
			if (state.getType() != ActivityType.Idle)
				return false;
		}
		return true;
	}

	public FlushResult flush(String username, String token) throws UnknownHostException {
		List<ActivityState> statesToSend = null;
		List<ActivityEvent> eventsToSend = null;
		DateTime batchStart = currentBatchStart;
		DateTime batchEnd = DateTime.now();

		context.getLogger().log("Codealike activity flush started");
		context.getLogger().log("LastState:" + lastState);
		context.getLogger().log("LastEvent:" + lastEvent);
		context.getLogger().log("Has only idle:" + this.HasOnlyIdleState());

		// if lastState or lastEvent are null then there is no info to flush
		// so lets skip this attempt
		if (lastState == null || lastEvent == null || this.HasOnlyIdleState()) {
			context.getLogger().log("Codealike activity flush skipped");
			return FlushResult.Skip;
		}

		synchronized(this)
		{
			// should close current batch (setting up duration for last state and event)
			// then generate a new batch (creating a copy of last state and event)

			// close last state duration and prepare list to be sent
			lastState.closeDuration(batchEnd);
			statesToSend = new LinkedList<>(this.states);

			// close last event duration and prepare list to be sent
			lastEvent.closeDuration(batchEnd);
			eventsToSend = new LinkedList<>(this.events);

			// recreate state list for next batch
			this.lastState = this.lastState.recreate();
			this.states = new LinkedList<>();
			this.states.add(this.lastState);

			// recreate events list for next batch
			this.lastEvent = this.lastEvent.recreate();
			this.events = new LinkedList<>();
			this.events.add(this.lastEvent);

			currentBatchStart = DateTime.now();
		}

		context.getLogger().log("Codealike activity flush running for [States: " + statesToSend.size() + " - Events: " + eventsToSend.size() + "]");

		// creates an info procesor
		ActivityInfoProcessor processor = new ActivityInfoProcessor(statesToSend, eventsToSend, batchStart, batchEnd);

		List<ActivityInfo> activityInfoList = processor.getSerializableEntities(context.getMachineName(),
				context.getInstanceValue(), context.getIdeName(), context.getPluginVersion());

		FlushResult result = FlushResult.Succeded;
		for(ActivityInfo info : activityInfoList) {
			if (!info.isValid()) {
				continue;
			}
			File cacheFolder = context.getConfiguration().getCachePath();
			if (cacheFolder == null) {
				context.getLogger().logError("Could not access cache folder. It might not be created.");
				continue;
			}
			FlushResult intermediateResult = trySendEntries(info, username, token);
			if (intermediateResult == FlushResult.Succeded) {
				for (final File fileEntry : cacheFolder.listFiles()) {
					trySendEntriesOnFile(fileEntry.getName(), username, token);
			    }
				
				if (context.getConfiguration().getTrackSent()) {
					//String.format("%s%s%s%s", cacheFolder.getAbsolutePath(), File.separator, info.getBatchId(), ".sent");
					File historyFile = context.getConfiguration().getHistoryFile();
					FileOutputStream stream = null;
					try {
						stream = new FileOutputStream(historyFile);
						ObjectWriter writer = context.getJsonWriter();
						String json = writer.writeValueAsString(info);
						stream.write(json.getBytes(Charset.forName("UTF-8")));
					}
					catch (Exception e) {
						context.getLogger().logError(e, "There was a problem trying to store activity data locally.");
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
				//String filename = String.format("%s\\%s%s", cacheFolder.getAbsolutePath(), info.getBatchId(), activityLogExtension);
				File cacheFile = context.getConfiguration().getCacheFile();
				FileOutputStream stream = null;
				try {
					stream = new FileOutputStream(cacheFile);
					ObjectWriter writer = context.getJsonWriter();
					String json = writer.writeValueAsString(info);
					stream.write(json.getBytes(Charset.forName("UTF-8")));
				}
				catch (Exception e) {
					context.getLogger().logError(e, "There was a problem trying to store activity data locally.");
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

		context.getLogger().log("Codealike activity flush finished");
		return result;
	}

	private void trySendEntriesOnFile(String fileName, String username, String token) {
		try {
			FlushResult result = FlushResult.Skip;
			File fileEntry = new File(context.getConfiguration().getCachePath(), fileName);
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
				context.getLogger().logError(e, "There was a problem trying to send offline activity data to the server.");
				result = FlushResult.Report;
			} 
			catch (KeyManagementException e) {
				context.getLogger().logError(e, "Could not send data to remote server. There was a problem with SSL configuration.");
				result = FlushResult.Skip;
			}
			
			finally {
				switch (result) {
					case Succeded:
					{
						fileEntry.renameTo(new File(context.getConfiguration().getHistoryPath(), fileName));
					}
					case Report:
					{
						fileEntry.renameTo(new File(context.getConfiguration().getHistoryPath(), fileName + ".error"));
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
			context.getLogger().logError(t, "There was a problem trying to send offline activity data to the server.");
		}
	}

	private FlushResult trySendEntries(ActivityInfo info, String username, String token) {
		try {
			ApiClient client;
			try {
				client = ApiClient.tryCreateNew(username, token);
			}
			catch (KeyManagementException e) {
				context.getLogger().logError(e, "Could send activity to remote server. There was a problem with SSL configuration.");
				return FlushResult.Offline;
			}
			
			ApiResponse<Void> response = client.postActivityInfo(info);
			if (!response.success()) {
				context.getLogger().logWarn(String.format("There was a problem trying to send activity data to the server (Status: %s). "
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
			context.getLogger().logError(t, "There was a problem trying to send activity data to the server.");
			return FlushResult.Report;
		}
	}

	public enum FlushResult {
		Offline,
		Succeded,
		Report,
		Skip
	}
}
