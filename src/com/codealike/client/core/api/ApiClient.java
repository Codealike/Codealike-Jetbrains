package com.codealike.client.core.api;

import java.net.ConnectException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.security.cert.*;

import com.codealike.client.core.internal.dto.ActivityInfo;
import com.codealike.client.core.internal.dto.HealthInfo;
import com.codealike.client.core.internal.dto.ProfileInfo;
import com.codealike.client.core.internal.dto.SolutionContextInfo;
import com.codealike.client.core.internal.dto.UserConfigurationInfo;
import com.codealike.client.core.internal.dto.Version;
import com.codealike.client.core.internal.startup.PluginContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ApiClient {

	private static final String X_EAUTH_CLIENT_HEADER = "X-Eauth-Client";
	private static final String X_EAUTH_TOKEN_HEADER = "X-Api-Token";
	public static final String X_EAUTH_IDENTITY_HEADER = "X-Api-Identity";
	public static final int MAX_RETRIES = 5;

	private WebTarget apiTarget;
	private String identity;
	private String token;

	public static ApiClient tryCreateNew(String identity, String token) throws KeyManagementException {
		return new ApiClient(identity, token);
	}

	public static ApiClient tryCreateNew() throws KeyManagementException {
		return new ApiClient();
	}

	protected ApiClient() throws KeyManagementException {
		 
		ClientBuilder builder = ClientBuilder.newBuilder();

		TrustManager[] certs = new TrustManager[] { new  javax.net.ssl.X509TrustManager() { 
            @Override 
            public X509Certificate[] getAcceptedIssuers() { 
                    return new X509Certificate[] {}; 
            } 


            @Override 
            public void checkServerTrusted(X509Certificate[] chain, 
                            String authType) throws CertificateException { 
            } 


            @Override 
            public void checkClientTrusted(X509Certificate[] chain, 
                            String authType) throws CertificateException { 
            } 
    } }; 
		
		SSLContext sslContext=null;
		 try {
		 sslContext = SSLContext.getInstance("SSL");
		 sslContext.init(null, certs, new SecureRandom());
		 } catch (NoSuchAlgorithmException e1) {
			 e1.printStackTrace();
		 }

		builder.sslContext(sslContext).hostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
		
		Client client = builder.build();
		apiTarget = client
				.target(PluginContext.getInstance().getProperty(
						"codealike.server.url")).path("/api/v2/");
		this.identity = "";
		this.token = "";
	}

	protected ApiClient(String identity, String token) throws KeyManagementException {
		this();
		if (identity != null && token != null) {
			this.identity = identity;
			this.token = token;
		}
	}

	public ApiResponse<Void> health() {
		try {
			WebTarget target = apiTarget.path("health");

			Invocation.Builder invocationBuilder = target
					.request(MediaType.APPLICATION_JSON);
			Response response = invocationBuilder.get();
			return new ApiResponse<Void>(response.getStatus(), response
					.getStatusInfo().getReasonPhrase());
		} catch (ProcessingException e) {
			if (e.getCause() != null
					&& e.getCause() instanceof ConnectException) {
				return new ApiResponse<Void>(ApiResponse.Status.ConnectionProblems);
			} else {
				return new ApiResponse<Void>(ApiResponse.Status.ClientError);
			}
		}
	}
	
	public ApiResponse<Void> logHealth(HealthInfo healthInfo) {
		try {
			WebTarget target = apiTarget.path("health");

			ObjectWriter writer = PluginContext.getInstance().getJsonWriter();
			String healthInfoLog = writer.writeValueAsString(healthInfo);
			
			Invocation.Builder invocationBuilder = target.request().accept(
					MediaType.APPLICATION_JSON);
			addHeaders(invocationBuilder);
			
			Response response = null;
			try {
				response = invocationBuilder.put(Entity.entity(healthInfoLog,
						MediaType.APPLICATION_JSON));
			} catch (Exception e) {
				return new ApiResponse<Void>(ApiResponse.Status.ConnectionProblems);
			}
			return new ApiResponse<Void>(response.getStatus(), response
					.getStatusInfo().getReasonPhrase());
		} catch (JsonProcessingException e) {
			return new ApiResponse<Void>(ApiResponse.Status.ClientError,
					String.format("Problem parsing data from the server. %s",
							e.getMessage()));
		}
	}

	public ApiResponse<Version> version() {
		WebTarget target = apiTarget.path("version").queryParam("client", "intellij");
		return doGet(target, Version.class);
	}

	public ApiResponse<SolutionContextInfo> getSolutionContext(UUID projectId) {
		WebTarget target = apiTarget.path("solution").path(projectId.toString());
		return doGet(target, SolutionContextInfo.class);
	}
	
	private <T> ApiResponse<T> doGet(WebTarget target, Class<T> type)
	{
		Invocation.Builder invocationBuilder = target.request(
				MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		addHeaders(invocationBuilder);

		try {
			Response response = null;
			try {
				response = invocationBuilder.get();
			} catch (Exception e) {
				return new ApiResponse<T>(ApiResponse.Status.ConnectionProblems);
			}

			if (response.getStatusInfo().getStatusCode() == Response.Status.OK
					.getStatusCode()) {
				String solutionContextInfoSerialized = response
						.readEntity(String.class);
				ObjectMapper mapper = PluginContext.getInstance()
						.getJsonMapper();
				T contextInfo = mapper.readValue(
						solutionContextInfoSerialized,
						type);
				if (contextInfo != null) {
					return new ApiResponse<T>(
							response.getStatus(), response.getStatusInfo()
									.getReasonPhrase(), contextInfo);
				} else {
					return new ApiResponse<T>(ApiResponse.Status.ClientError,
							"Problem parsing data from the server.");
				}
			} else {
				return new ApiResponse<T>(response.getStatus(), response
						.getStatusInfo().getReasonPhrase());
			}
		} catch (Exception e) {
			return new ApiResponse<T>(ApiResponse.Status.ClientError,
					String.format("Problem parsing data from the server. %s",
							e.getMessage()));
		}
	}
	
	public ApiResponse<ProfileInfo> getProfile(String username) {
		WebTarget target = apiTarget.path("account").path(username).path("profile");
		return doGet(target, ProfileInfo.class);
	}
	
	public ApiResponse<UserConfigurationInfo> getUserConfiguration(String username) {
		WebTarget target = apiTarget.path("account").path(username).path("config");
		return doGet(target, UserConfigurationInfo.class);
	}

	public ApiResponse<Void> registerProjectContext(UUID projectId, String name) {
		try {
			SolutionContextInfo solutionContext = new SolutionContextInfo(
					projectId, name);
			WebTarget target = apiTarget.path("solution");

			ObjectWriter writer = PluginContext.getInstance().getJsonWriter();
			String solutionAsJson = writer.writeValueAsString(solutionContext);
			Invocation.Builder invocationBuilder = target.request().accept(
					MediaType.APPLICATION_JSON);
			addHeaders(invocationBuilder);

			Response response = null;
			try {
				response = invocationBuilder.post(Entity.entity(solutionAsJson,
						MediaType.APPLICATION_JSON));
			} catch (Exception e) {
				return new ApiResponse<Void>(ApiResponse.Status.ConnectionProblems);
			}
			return new ApiResponse<Void>(response.getStatus(), response
					.getStatusInfo().getReasonPhrase());
		} catch (JsonProcessingException e) {
			return new ApiResponse<Void>(ApiResponse.Status.ClientError,
					String.format("Problem parsing data from the server. %s",
							e.getMessage()));
		}
	}

	public ApiResponse<Void> postActivityInfo(ActivityInfo info) {
		try {
			WebTarget target = apiTarget.path("activity");

			ObjectWriter writer = PluginContext.getInstance().getJsonWriter();
			String activityInfoAsJson = writer.writeValueAsString(info);
			Invocation.Builder invocationBuilder = target.request().accept(
					MediaType.APPLICATION_JSON);
			addHeaders(invocationBuilder);

			Response response = null;
			try {
				response = invocationBuilder.post(Entity.entity(
						activityInfoAsJson, MediaType.APPLICATION_JSON));
			} catch (Exception e) {
				return new ApiResponse<Void>(ApiResponse.Status.ConnectionProblems);
			}
			return new ApiResponse<Void>(response.getStatus(), response
					.getStatusInfo().getReasonPhrase());
		} catch (JsonProcessingException e) {
			return new ApiResponse<Void>(ApiResponse.Status.ClientError,
					String.format("Problem parsing data from the server. %s",
							e.getMessage()));
		}
	}

	private void addHeaders(Invocation.Builder invocationBuilder) {
		invocationBuilder.header(X_EAUTH_IDENTITY_HEADER, this.identity);
		invocationBuilder.header(X_EAUTH_TOKEN_HEADER, this.token);
		invocationBuilder.header(X_EAUTH_CLIENT_HEADER, "intellij");
	}

	public ApiResponse<Void> tokenAuthenticate() {
		WebTarget target = apiTarget.path("account").path(this.identity)
				.path("authorized");
		Invocation.Builder invocationBuilder = target.request(
				MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

		addHeaders(invocationBuilder);

		Response response = null;
		try {
			response = invocationBuilder.get();
		} catch (Exception e) {
			return new ApiResponse<Void>(ApiResponse.Status.ConnectionProblems);
		}
		return new ApiResponse<Void>(response.getStatus(), response.getStatusInfo()
				.getReasonPhrase());
	}
}
