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
	private List<ActivityState> states;
	private List<ActivityEvent> events;

	private ActivityEvent lastEvent;
	private ActivityState lastState;

	private PluginContext context;

	private DateTime currentBatchStart;
	
	public ActivitiesRecorder(PluginContext context)
	{
		this.states = new LinkedList<>();
		this.events = new LinkedList<>();
		this.context = context;
		this.currentBatchStart = DateTime.now();
	}
	
	public synchronized ActivityState recordState(ActivityState state) {

		if (lastState == null) {
			// add the state to states list and set as lastRecordedState
			lastState = state;
			this.states.add(lastState);
		}
		else {
			// have to set last state duration to now
			lastState.setDuration(new Period(lastState.getCreationTime(), DateTime.now()));

			// if new state is equal to last recorded state
			if (!lastState.equals(state)) {
				// if it is a different state, close last state duration and add new state
				lastState = state;
				this.states.add(lastState);
			}
		}

		TrackingConsole.getInstance().trackState(lastState);

		// and return
		return lastState;
	}

	public synchronized ActivityEvent recordEvent(ActivityEvent event) {

		if (lastEvent == null) {
			// add the state to states list and set as lastRecordedState
			lastEvent = event;
			this.events.add(lastEvent);
		}
		else {
			// have to set last state duration to now
			lastEvent.setDuration(new Period(lastEvent.getCreationTime(), DateTime.now()));

			// if both events are equivalents mean 'event' is continuation from 'lastEvent'
			// in this case, we only update duration and context from last event
			// duration because the event is still the same (coding on certain line of file)
			// and context because last event on same line will gave us the final version of the
			// context
			if (lastEvent.isEquivalent(event)) {
				lastEvent.setContext(event.getContext());
			}
			else {
				// if it is a different event, close last event duration and add new state
				lastEvent = event;
				this.events.add(lastEvent);
			}
		}

		TrackingConsole.getInstance().trackEvent(lastEvent);

		// and return
		return lastEvent;
	}

	public FlushResult flush(String username, String token) throws UnknownHostException {
		List<ActivityState> statesToSend = null;
		List<ActivityEvent> eventsToSend = null;
		DateTime batchStart = currentBatchStart;
		DateTime batchEnd = DateTime.now();

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
			this.states = new LinkedList<>();
			this.recordState(lastState.recreate());

			// recreate events list for next batch
			this.events = new LinkedList<>();
			this.recordEvent(lastEvent.recreate());

			currentBatchStart = DateTime.now();
		}

		// creates an info procesor
		ActivityInfoProcessor processor = new ActivityInfoProcessor(statesToSend, eventsToSend, batchStart, batchEnd);

		List<ActivityInfo> activityInfoList = processor.getSerializableEntities(context.getMachineName(),
				context.getInstanceValue(), context.getIdeName(), context.getPluginVersion());
		String activityLogExtension = context.getProperty("activity-log.extension");


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
					String filename = String.format("%s%s%s%s", cacheFolder.getAbsolutePath(), File.separator, info.getBatchId(), ".sent");
					
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

	public enum FlushResult {
		Offline,
		Succeded,
		Report,
		Skip
	}
}
