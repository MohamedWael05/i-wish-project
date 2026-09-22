package com.iwish.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private String action;
    private Map<String, Serializable> params = new HashMap<>();

    public Request() {}

    public Request(String action) {
        this.action = action;
    }

    public Request put(String key, Serializable value) {
        params.put(key, value);
        return this;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Map<String, Serializable> getParams() { return params; }
    public void setParams(Map<String, Serializable> params) { this.params = params; }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) params.get(key);
    }

    public String getString(String key) {
        Object o = params.get(key);
        return o == null ? null : o.toString();
    }

    public int getInt(String key) {
        Object o = params.get(key);
        return o == null ? 0 : Integer.parseInt(o.toString());
    }
}
