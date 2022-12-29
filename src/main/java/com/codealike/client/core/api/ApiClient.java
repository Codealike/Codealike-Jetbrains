/*
 * Copyright (c) 2022. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.api;

import java.net.ConnectException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

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
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Api class to communicate with Codealike server.
 *
 * @author Daniel, pvmagacho
 * @version 1.5.0.2
 */
public class ApiClient {

    // Headers for API authentication
    private static final String X_EAUTH_CLIENT_HEADER = "X-Eauth-Client";
    private static final String X_EAUTH_TOKEN_HEADER = "X-Api-Token";
    public static final String X_EAUTH_IDENTITY_HEADER = "X-Api-Identity";

    // Number of API retries
    public static final int MAX_RETRIES = 5;

    private WebTarget apiTarget;
    private String identity;
    private String token;

    /**
     * Create a new API client.
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
     * Create a new API client.
     *
     * @return the created APIClient instance
     * @throws KeyManagementException if any error with token occurs
     */
    public static ApiClient tryCreateNew() throws KeyManagementException {
        return new ApiClient();
    }

    /**
     * API Client constructor.
     *
     * @throws KeyManagementException if any error with token occurs
     */
    protected ApiClient() throws KeyManagementException {
        ClientBuilder builder = ClientBuilder.newBuilder();

        TrustManager[] certs = new TrustManager[]{
                new javax.net.ssl.X509TrustManager() {
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
                }
        };

        SSLContext sslContext = null;
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

    /**
     * Updates health information.
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
     * Register project being tracked.
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

    /**
     * Do an account authentication using token.
     *
     * @return the {@link ApiResponse} instance
     */
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
}
