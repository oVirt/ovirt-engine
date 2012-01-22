package org.ovirt.engine.api.restapi.rsdl;

import java.util.HashMap;
import java.util.Map;

public class Request {

    private Body body;
    private Map<String, UrlParamData> urlparams = new HashMap<String, UrlParamData>();
    private Map<String, String> headers = new HashMap<String, String>();

    public Map<String, UrlParamData> getUrlparams() {
        return urlparams;
    }
    public void setUrlparams(Map<String, UrlParamData> urlparams) {
        this.urlparams = urlparams;
    }
    public Body getBody() {
        return body;
    }
    public void setBody(Body body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    public void addHeader(String name, String type) {
        this.headers.put(name, type);
    }
    public void addUrl_params(String name, UrlParamData value) {
        this.urlparams.put(name, value);
    }
}
