package com.codealike.client.core.api;

public class ApiResponse<T> {

    private Status status;
    private String message;
    private T object;

    public ApiResponse(Status status) {
        this.status = status;
    }

    public ApiResponse(Status status, String message) {
        this.status = status;
    }

    public ApiResponse(int status, String message, T object) {
        this.status = Status.fromStatusCode(status);
        this.object = object;
        this.message = message;
    }

    public ApiResponse(int status, String message) {
        this.status = Status.fromStatusCode(status);
        this.message = message;
    }

    public Boolean success() {
        return this.status == Status.Ok;
    }

    public T getObject() {
        return object;
    }

    public boolean notFound() {
        return this.status == Status.NotFound;
    }

    public String getMessage() {
        return message;
    }

    public boolean connectionTimeout() {
        return this.status == Status.ConnectionProblems;
    }

    public boolean conflict() {
        return this.status == Status.Conflict;
    }

    public Status getStatus() {
        return this.status;
    }

    public boolean error() {
        return this.status == Status.InternalServerError || this.status == Status.ClientError;
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
                case 500:
                    return InternalServerError;
                case 400:
                    return BadRequest;
                case 409:
                    return Conflict;
                case 503:
                    return ServiceUnavailable;
                case 401:
                    return Unauthorized;
                default:
                    return InternalServerError;
            }
        }

    }


}