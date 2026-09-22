package com.iwish.common;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Serializable data;

    public Response() {}

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Response(boolean success, String message, Serializable data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static Response ok(String message) { return new Response(true, message); }
    public static Response ok(String message, Serializable data) { return new Response(true, message, data); }
    public static Response fail(String message) { return new Response(false, message); }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Serializable getData() { return data; }
    public void setData(Serializable data) { this.data = data; }
}
