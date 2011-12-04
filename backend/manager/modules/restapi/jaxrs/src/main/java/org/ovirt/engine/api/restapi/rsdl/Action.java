package org.ovirt.engine.api.restapi.rsdl;

import java.util.HashMap;
import java.util.Map;

public class Action {

    public Action() {
        super();
    }

    public Action(String name) {
        super();
        this.name = name;
        this.request = new Request();
    }

    private String name;
    private Request request;
    private Map<Object, Object> headers = new HashMap<Object, Object>();


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Map<Object, Object> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<Object, Object> headers) {
        this.headers = headers;
    }
    public Request getRequest() {
        return request;
    }
    public void setRequest(Request request) {
        this.request = request;
    }
    public void addHeader(String name, String type) {
        this.headers.put(name, type);
    }
}
