/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.api;

import com.codealike.client.core.internal.dto.*;
import com.codealike.client.core.internal.startup.PluginContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientProperties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.ConnectException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

/**
 * Api class to communicate with Codealike server.
 *
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class ApiClient {

    // Headers for API authentication
    private static final String X_EAUTH_CLIENT_HEADER = "X-Eauth-Client";
    private static final String X_EAUTH_TOKEN_HEADER = "X-Api-Token";
    public static final String X_EAUTH_IDENTITY_HEADER = "X-Api-Identity";
    // Number of API retries
    public static final int MAX_RETRIES = 5;
    private final WebTarget apiTarget;
    private String identity;
    private String token;

    /**
     * Create a new API client. Used to communicate with the Codealike remote server.
     *
     * @param identity the user identity
     * @param token    the user token
     * @return the created APIClient instance
     * @throws KeyManagementException if any error with token occurs
     */
    public static ApiClient tryCreateNew(String identity, String token) throws KeyManagementException {
        return new ApiClient(identity, token);
    }

    /**
     * Create a new API client. Used to communicate with the Codealike remote server.
     *
     * @return the created APIClient instance
     * @throws KeyManagementException if any error with token occurs
     */
    public static ApiClient tryCreateNew() throws KeyManagementException {
        return new ApiClient();
    }

    /**
     * API Client constructor. Used to communicate with the Codealike remote server.
     *
     * @throws KeyManagementException if any error with token occurs
     */
    protected ApiClient() throws KeyManagementException {
        ClientBuilder builder = ClientBuilder.newBuilder();
        TrustManager[] certs = new TrustManager[]{new javax.net.ssl.X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, certs, new SecureRandom());
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }

        builder.sslContext(sslContext).hostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());

        Client client = builder.build();
        client.property(ClientProperties.CONNECT_TIMEOUT, 30000);
        client.property(ClientProperties.READ_TIMEOUT, 5000);

        apiTarget = client.target(PluginContext.getInstance().getConfiguration().getApiUrl());
        this.identity = "";
        this.token = "";
    }

    /**
     * API Client constructor.
     *
     * @param identity the user identity
     * @param token    the user token
     * @throws KeyManagementException if any error with token occurs
     */
    protected ApiClient(String identity, String token) throws KeyManagementException {
        this();
        if (identity != null && token != null) {
            this.identity = identity;
            this.token = token;
        }
    }

    /**
     * Get the plugin settings from the remote server.
     *
     * @return the {@link ApiResponse} instance with {@link PluginSettingsInfo} information
     */
    public static ApiResponse<PluginSettingsInfo> getPluginSettings() {
        ObjectMapper mapper = new ObjectMapper();
        ClientBuilder builder = ClientBuilder.newBuilder();
        Client client = builder.build();
        client.property(ClientProperties.CONNECT_TIMEOUT, 30000);
        client.property(ClientProperties.READ_TIMEOUT, 5000);

        WebTarget pluginSettingsTarget = client.target("https://codealike.com/api/v2/public/PluginsConfiguration");

        Invocation.Builder invocationBuilder = pluginSettingsTarget.request(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        try {
            Response response;
            try {
                response = invocationBuilder.get();
            } catch (Exception e) {
                return new ApiResponse<>(ApiResponse.Status.ConnectionProblems);
            }

            if (response.getStatusInfo().getStatusCode() == Response.Status.OK.getStatusCode()) {
                // process response to get a valid json string representation
                String serializedObject = response.readEntity(String.class);
                String normalizedObject = serializedObject.substring(1, serializedObject.length() - 1).replace("\\", "");

                // parse the json object to get a valid plugin settings object
                PluginSettingsInfo pluginSettingsInfo = mapper.readValue(normalizedObject, PluginSettingsInfo.class);

                if (pluginSettingsInfo != null) {
                    return new ApiResponse<>(
                            response.getStatus(), response.getStatusInfo()
                            .getReasonPhrase(), pluginSettingsInfo);
                } else {
                    return new ApiResponse<>(ApiResponse.Status.ClientError,
                            "Problem parsing data from the server.");
                }
            } else {
                return new ApiResponse<>(response.getStatus(), response
                        .getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            return new ApiResponse<>(ApiResponse.Status.ClientError,
                    String.format("Problem parsing data from the server. %s",
                            e.getMessage()));
        }
    }

    /**
     * Check API health.
     *
     * @return the {@link ApiResponse} instance
     */
    public ApiResponse<Void> health() {
        try {
            WebTarget target = apiTarget.path("health");

            Invocation.Builder invocationBuilder = target
                    .request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();
            return new ApiResponse<>(response.getStatus(), response
                    .getStatusInfo().getReasonPhrase());
        } catch (ProcessingException e) {
            if (e.getCause() != null
                    && e.getCause() instanceof ConnectException) {
                return new ApiResponse<>(ApiResponse.Status.ConnectionProblems);
            } else {
                return new ApiResponse<>(ApiResponse.Status.ClientError);
            }
        }
    }

    /**
     * Log the plugin health information to the remote server.
     *
     * @param healthInfo the health information object to update
     * @return the {@link ApiResponse} instance
     */
    public ApiResponse<Void> logHealth(HealthInfo healthInfo) {
        try {
            WebTarget target = apiTarget.path("health");

            ObjectWriter writer = PluginContext.getInstance().getJsonWriter();
            String healthInfoLog = writer.writeValueAsString(healthInfo);

            Invocation.Builder invocationBuilder = target.request().accept(
                    MediaType.APPLICATION_JSON);
            addHeaders(invocationBuilder);

            Response response;
            try {
                response = invocationBuilder.put(Entity.entity(healthInfoLog,
                        MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                return new ApiResponse<>(ApiResponse.Status.ConnectionProblems);
            }
            return new ApiResponse<>(response.getStatus(), response
                    .getStatusInfo().getReasonPhrase());
        } catch (JsonProcessingException e) {
            return new ApiResponse<>(ApiResponse.Status.ClientError,
                    String.format("Problem parsing data from the server. %s",
                            e.getMessage()));
        }
    }

    /**
     * Get version for intellij plugin.
     *
     * @return the {@link ApiResponse} instance with {@link Version} information
     */
    public ApiResponse<Version> version() {
        WebTarget target = apiTarget.path("version").queryParam("client", "intellij");
        return doGet(target, Version.class);
    }

    /**
     * Get solution context information.
     *
     * @param projectId the current project id being tracker
     * @return the {@link ApiResponse} instance with {@link SolutionContextInfo} information
     */
    public ApiResponse<SolutionContextInfo> getSolutionContext(UUID projectId) {
        WebTarget target = apiTarget.path("solution").path(projectId.toString());
        return doGet(target, SolutionContextInfo.class);
    }

    /**
     * Get project information.
     *
     * @param username the profile username
     * @return the {@link ApiResponse} instance with {@link ProfileInfo} information
     */
    public ApiResponse<ProfileInfo> getProfile(String username) {
        WebTarget target = apiTarget.path("account").path(username).path("profile");
        return doGet(target, ProfileInfo.class);
    }

    /**
     * Get user configuration information.
     *
     * @param username the profile username
     * @return the {@link ApiResponse} instance with {@link UserConfigurationInfo} information
     */
    public ApiResponse<UserConfigurationInfo> getUserConfiguration(String username) {
        WebTarget target = apiTarget.path("account").path(username).path("config");
        return doGet(target, UserConfigurationInfo.class);
    }

    /**
     * Register the project being tracked with the remote server.
     *
     * @param projectId the project identifier to track
     * @param name      the project name
     * @return the {@link ApiResponse} instance
     */
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

            Response response;
            try {
                response = invocationBuilder.post(Entity.entity(solutionAsJson,
                        MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                return new ApiResponse<>(ApiResponse.Status.ConnectionProblems);
            }
            return new ApiResponse<>(response.getStatus(), response
                    .getStatusInfo().getReasonPhrase());
        } catch (JsonProcessingException e) {
            return new ApiResponse<>(ApiResponse.Status.ClientError,
                    String.format("Problem parsing data from the server. %s",
                            e.getMessage()));
        }
    }

    /**
     * Post project activity information.
     *
     * @param info the activity information object
     * @return the {@link ApiResponse} instance
     */
    public ApiResponse<Void> postActivityInfo(ActivityInfo info) {
        try {
            WebTarget target = apiTarget.path("activity");

            ObjectWriter writer = PluginContext.getInstance().getJsonWriter();
            String activityInfoAsJson = writer.writeValueAsString(info);
            Invocation.Builder invocationBuilder = target.request().accept(
                    MediaType.APPLICATION_JSON);
            addHeaders(invocationBuilder);

            Response response;
            try {
                response = invocationBuilder.post(Entity.entity(
                        activityInfoAsJson, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                return new ApiResponse<>(ApiResponse.Status.ConnectionProblems);
            }
            return new ApiResponse<>(response.getStatus(), response
                    .getStatusInfo().getReasonPhrase());
        } catch (JsonProcessingException e) {
            return new ApiResponse<>(ApiResponse.Status.ClientError,
                    String.format("Problem parsing data from the server. %s",
                            e.getMessage()));
        }
    }

    /**
     * Do an account authentication using the Codealike token.
     *
     * @return the {@link ApiResponse} instance
     */
    public ApiResponse<Void> tokenAuthenticate() {
        WebTarget target = apiTarget.path("account").path(this.identity)
                .path("authorized");
        Invocation.Builder invocationBuilder = target.request(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        addHeaders(invocationBuilder);

        Response response;
        try {
            response = invocationBuilder.get();
        } catch (Exception e) {
            return new ApiResponse<>(ApiResponse.Status.ConnectionProblems);
        }
        return new ApiResponse<>(response.getStatus(), response.getStatusInfo()
                .getReasonPhrase());
    }

    /**
     * Private method to add headers to request.
     */
    private void addHeaders(Invocation.Builder invocationBuilder) {
        invocationBuilder.header(X_EAUTH_IDENTITY_HEADER, this.identity);
        invocationBuilder.header(X_EAUTH_TOKEN_HEADER, this.token);
        invocationBuilder.header(X_EAUTH_CLIENT_HEADER, "intellij");
    }

    /**
     * Private method to do an API GET.
     */
    private <T> ApiResponse<T> doGet(WebTarget target, Class<T> type) {
        Invocation.Builder invocationBuilder = target.request(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        addHeaders(invocationBuilder);

        try {
            Response response;
            try {
                response = invocationBuilder.get();
            } catch (Exception e) {
                return new ApiResponse<>(ApiResponse.Status.ConnectionProblems);
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
                    return new ApiResponse<>(
                            response.getStatus(), response.getStatusInfo()
                            .getReasonPhrase(), contextInfo);
                } else {
                    return new ApiResponse<>(ApiResponse.Status.ClientError,
                            "Problem parsing data from the server.");
                }
            } else {
                return new ApiResponse<>(response.getStatus(), response
                        .getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            return new ApiResponse<>(ApiResponse.Status.ClientError,
                    String.format("Problem parsing data from the server. %s",
                            e.getMessage()));
        }
    }
}
