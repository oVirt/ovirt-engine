package org.ovirt.engine.api.restapi.rsdl;

import java.util.HashMap;
import java.util.Map;

public class Request {

    private Body body;
    private Map<Object, Object> urlparams = new HashMap<Object, Object>();
    private Map<Object, Object> headers = new HashMap<Object, Object>();

    public Map<Object, Object> getUrlparams() {
        return urlparams;
    }
    public void setUrlparams(Map<Object, Object> urlparams) {
        this.urlparams = urlparams;
    }
    public Body getBody() {
        return body;
    }
    public void setBody(Body body) {
        this.body = body;
    }

    public Map<Object, Object> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<Object, Object> headers) {
        this.headers = headers;
    }
    public void addHeader(String name, String type) {
        this.headers.put(name, type);
    }
    public void addUrl_params(String name, String value) {
        this.urlparams.put(name, value);
    }
}
