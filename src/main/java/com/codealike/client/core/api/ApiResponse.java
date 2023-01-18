/*
 * Copyright (c) 2022-2023. All rights reserved to Torc LLC.
 */
package com.codealike.client.core.api;

/**
 * Codealike API response class.
 *
 * @param <T> the respose type.
 * @author Daniel, pvmagacho
 * @version 1.6.0.0
 */
public class ApiResponse<T> {
    // The response HTTP status
    private final Status status;
    // The response HTTP message
    private String message;
    // The response HTTP object (in JSON format)
    private T object;

    /**
     * API response constructor.
     *
     * @param status the API status object
     */
    public ApiResponse(Status status) {
        this.status = status;
    }

    /**
     * API response constructor.
     *
     * @param status  the API status object
     * @param message the API message
     */
    public ApiResponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * API response constructor.
     *
     * @param status  the API status value
     * @param message the API message
     * @param object  the API response object
     */
    public ApiResponse(int status, String message, T object) {
        this.status = Status.fromStatusCode(status);
        this.object = object;
        this.message = message;
    }

    /**
     * API response constructor.
     *
     * @param status  the API status value
     * @param message the API message
     */
    public ApiResponse(int status, String message) {
        this.status = Status.fromStatusCode(status);
        this.message = message;
    }

    /**
     * Check if response was successful.
     *
     * @return true if response was successful, false otherwise
     */
    public Boolean success() {
        return this.status == Status.Ok;
    }

    /**
     * Check if response had not found status.
     *
     * @return true if response had not found status, false otherwise
     */
    public boolean notFound() {
        return this.status == Status.NotFound;
    }

    /**
     * Check if response had connection problems.
     *
     * @return true if response had connection problems, false otherwise
     */
    public boolean connectionTimeout() {
        return this.status == Status.ConnectionProblems;
    }

    /**
     * Check if response had conflict status.
     *
     * @return true if response had conflict status, false otherwise
     */
    public boolean conflict() {
        return this.status == Status.Conflict;
    }

    /**
     * Check if response had internal server error.
     *
     * @return true if response had internal server error, false otherwise
     */
    public boolean error() {
        return this.status == Status.InternalServerError || this.status == Status.ClientError;
    }

    public Status getStatus() {
        return this.status;
    }

    public T getObject() {
        return object;
    }

    public String getMessage() {
        return message;
    }

    public enum Status {
        Ok,
        BadRequest,
        ConnectionProblems,
        ServiceUnavailable,
        InternalServerError,
        Conflict,
        NotFound,
        Unauthorized,
        ClientError;

        static Status fromStatusCode(int code) {
            switch (code) {
                case 200:
                    return Ok;
                case 404:
                    return NotFound;
                case 400:
                    return BadRequest;
                case 401:
                    return Unauthorized;
                case 409:
                    return Conflict;
                case 503:
                    return ServiceUnavailable;
                default:
                    return InternalServerError;
            }
        }

    }


}
