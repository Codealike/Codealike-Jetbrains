package com.codealike.client.core.internal.startup;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.util.Random;
import java.util.UUID;

import com.codealike.client.core.internal.dto.PluginSettingsInfo;
import com.codealike.client.core.internal.model.ProjectSettings;
import com.codealike.client.core.internal.services.LoggerService;
import com.codealike.client.core.internal.utils.Configuration;
import com.codealike.client.intellij.ProjectConfig;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.PlatformUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import com.codealike.client.core.api.ApiClient;
import com.codealike.client.core.api.ApiResponse;
import com.codealike.client.core.internal.dto.SolutionContextInfo;
import com.codealike.client.core.internal.dto.Version;
import com.codealike.client.core.internal.serialization.JodaPeriodModule;
import com.codealike.client.core.internal.services.IdentityService;
import com.codealike.client.core.internal.services.TrackingService;
import com.codealike.client.core.internal.tracking.code.ContextCreator;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@SuppressWarnings("restriction")
public class PluginContext {
	public static final String VERSION = "1.5.0.26";
	private static final String PLUGIN_PREFERENCES_QUALIFIER = "com.codealike.client.intellij";
	private static PluginContext _instance;

	private String ideName;
	private Version protocolVersion;
	private ObjectWriter jsonWriter;
	private ObjectMapper jsonMapper;
	private ContextCreator contextCreator;

	private DateTimeFormatter dateTimeFormatter;
	private DateTimeFormatter dateTimeParser;
	private IdentityService identityService;
	private TrackingService trackingService;
	private LoggerService loggerService;
	private String instanceValue;
	private String machineName;

	private Configuration configuration;
	
	public static final UUID UNASSIGNED_PROJECT = UUID.fromString("00000000-0000-0000-0000-0000000001");

	public LoggerService getLogger() {
		return this.loggerService;
	}

	public static PluginContext getInstance() {
			if (_instance == null)
			{
				_instance = new PluginContext();
			}
		return _instance;
	}
	
	public PluginContext() {
		DateTimeZone.setDefault(DateTimeZone.UTC);

		this.ideName = PlatformUtils.getPlatformPrefix();
		this.instanceValue = String.valueOf(new Random(DateTime.now().getMillis()).nextInt(Integer.MAX_VALUE) + 1);

		// initialize codealike configuration and load global settings
		this.configuration = new Configuration(this.ideName, VERSION, this.instanceValue);

		// load user preferences from global settings file
		this.configuration.loadGlobalSettings();

		// initialize logger
		this.loggerService = new LoggerService(configuration);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JodaPeriodModule());
		mapper.setSerializationInclusion(Include.NON_NULL);
		this.jsonWriter = mapper.writer().withDefaultPrettyPrinter();
		this.jsonMapper = mapper;
		this.contextCreator = new ContextCreator();
		this.dateTimeParser = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		this.dateTimeFormatter = new DateTimeFormatterBuilder().appendYear(4, 4).appendLiteral("-").
				appendMonthOfYear(2).appendLiteral("-").appendDayOfMonth(2).
				appendLiteral("T").appendHourOfDay(2).appendLiteral(":").
				appendMinuteOfHour(2).appendLiteral(":").appendSecondOfMinute(2).
				appendLiteral(".").appendMillisOfSecond(3).appendLiteral("Z").toFormatter();
		this.identityService = IdentityService.getInstance(this.loggerService);

		this.protocolVersion = new Version(0, 9);

		this.machineName = findLocalHostNameOr("unknown");
		this.loggerService.log("Codealike initialized with host name " + this.machineName);

		// try to load plugin settings from server
		ApiResponse<PluginSettingsInfo> pluginSettings = ApiClient.getPluginSettings(this.loggerService);
		if (pluginSettings.success()) {
			this.loggerService.log("Plugin settings retrieved");
			this.configuration.loadPluginSettings(pluginSettings.getObject());
		}
		else {
			this.loggerService.log("Plugin settings could not been retrieved");
		}
	}

	public Configuration getConfiguration() {
		return this.configuration;
	}

	public String getIdeName() {
		return this.ideName;
	}

	public String getPluginVersion() {
		return VERSION;
	}

	public String getMachineName() {
		return machineName;
	}

	public void initializeContext() throws IOException {
		this.trackingService = TrackingService.getInstance();
	}

	private UUID tryGetLegacySolutionIdV2(Project project) {
		UUID solutionId = null;

		// load project configuration
		ProjectConfig config = ProjectConfig.getInstance(project);

		// get solution id from codealike.xml file
		// saved inside .idea folder
		solutionId = config.getProjectId();

		// if solution id is not present in codealike.xml
		if (solutionId == null) {
			// try to get solution id from legacy store
			// first versions of the plugin saved solutionId
			// in a local non romeable store
			solutionId = tryGetLegacySolutionIdV1(project);

			// if solutionId is still null
			// there is no clue of the solution id
			// so it should be a new one. Let's create
			// a new solutionId
			if (solutionId == null) {
				solutionId = tryCreateUniqueId();

				try {
					// once created, let's register the solution id for the project
					if (!registerProjectContext(solutionId, project.getName())) {
						return null;
					}
				} catch (Exception e) {
					String projectName = project != null ? project.getName() : "";
					this.loggerService.logError(e, "Could not create UUID for project "+projectName);
				}
			}
		}

		return solutionId;
	}

	private UUID tryGetLegacySolutionIdV1(Project project) {
		UUID solutionId = null;

		try {
			PropertiesComponent projectNode = PropertiesComponent.getInstance(project);
			String solutionIdString = projectNode.getValue("codealike.solutionId", "");

			if (solutionIdString != "") {
				solutionId = UUID.fromString(solutionIdString);
			}
		}
		catch(Exception e) {
			String projectName = project != null ? project.getName() : "";
			this.loggerService.logError(e, "Could not retrieve solution id from legacy store " + projectName);
		}

		return solutionId;
	}

	public UUID getOrCreateUUID(Project project) {
		Configuration configuration = PluginContext.getInstance().getConfiguration();
		UUID solutionId = null;

		// try first to load codealike.json file from project folder
		ProjectSettings projectSettings = configuration.loadProjectSettings(project.getBasePath());

		if (projectSettings.getProjectId() == null) {
			// if configuration was not found in the expected place
			// let's try to load configuration from older plugin versions
			solutionId = tryGetLegacySolutionIdV2(project);

			if (solutionId != null) {
				// if solution id was found by other method than
				// loading project settings file from project folder
				// we have to save a new project settings with
				// generated information
				projectSettings.setProjectId(solutionId);
				projectSettings.setProjectName(project.getName());

				// and save the file for future uses
				configuration.saveProjectSettings(project.getBasePath(), projectSettings);
			}
			else {
				// if we reached this branch
				// it means not only no configuration was found
				// but also we were not able to register a new
				// configuration in server.
				// log was saved by internal method
				// nothing else to do here
			}
		}

		return projectSettings.getProjectId();
	}

	private String findLocalHostNameOr(String defaultName) {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) { //see: http://stackoverflow.com/a/40702767/1117552
			return defaultName;
		}
	}
	
	private UUID tryCreateUniqueId() {
		UUID solutionId = UUID.randomUUID();
		ApiClient client;
		try {
			client = ApiClient.tryCreateNew(this.identityService.getIdentity(), this.identityService.getToken());
		}
		catch (KeyManagementException e) {
			this.loggerService.logError(e, "Could not create unique Id synchronized with the server. There was a problem with SSL configuration.");
			return solutionId;
		}
		ApiResponse<SolutionContextInfo> response = client.getSolutionContext(solutionId);
		if (response.connectionTimeout()) {
			this.loggerService.logInfo("Communication problems running in offline mode.");
			return solutionId;
		}
		int numberOfRetries = 0;
		while (response.conflict() || (response.error() && numberOfRetries < ApiClient.MAX_RETRIES)) {
			solutionId = UUID.randomUUID();
			response = client.getSolutionContext(solutionId);
		}
		
		return solutionId;
	}
	
	public boolean registerProjectContext(UUID solutionId, String projectName) throws Exception {
		ApiClient client;
		try {
			client = ApiClient.tryCreateNew(this.identityService.getIdentity(), this.identityService.getToken());
		}
		catch (KeyManagementException e) {
			this.loggerService.logError(e, "Could not register unique project context in the remote server. There was a problem with SSL configuration.");
			return false;
		}
		ApiResponse<SolutionContextInfo> solutionInfoResponse = client.getSolutionContext(solutionId);
		if (solutionInfoResponse.notFound()) {
			ApiResponse<Void> response = client.registerProjectContext(solutionId, projectName);
			if (!response.success()) {
				this.loggerService.logError("Problem registering solution.");
			}
			else {
				return true;
			}
		}
		else if (solutionInfoResponse.success()) {
			return true;
		}
		else if (solutionInfoResponse.connectionTimeout()) {
			this.loggerService.logInfo("Communication problems running in offline mode.");
		}
		return false;
	}

	public ObjectWriter getJsonWriter() {
		return this.jsonWriter;
	}
	
	public ObjectMapper getJsonMapper() {
		return this.jsonMapper;
	}

	public ContextCreator getContextCreator() {
		return this.contextCreator;
	}

	public DateTimeFormatter getDateTimeFormatter() {
		return this.dateTimeFormatter;
	}

	public DateTimeFormatter getDateTimeParser() {
		return this.dateTimeParser;
	}

	public IdentityService getIdentityService() {
		return identityService;
	}

	public boolean isAuthenticated() {
		return this.identityService.isAuthenticated();
	}

	public TrackingService getTrackingService() {
		return trackingService;
	}

	public String getInstanceValue() {
		return instanceValue;
	}
}
