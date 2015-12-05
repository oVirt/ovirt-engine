package org.ovirt.engine.api.rsdl;

import java.util.HashMap;
import java.util.Map;

public class Request {

    private Body body;
    private Map<String, ParamData> urlparams = new HashMap<>();
    private Map<String, ParamData> headers = new HashMap<>();

    public Map<String, ParamData> getUrlparams() {
        return urlparams;
    }
    public void setUrlparams(Map<String, ParamData> urlparams) {
        this.urlparams = urlparams;
    }

    public Map<String, ParamData> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<String, ParamData> urlparams) {
        this.headers = urlparams;
    }

    public Body getBody() {
        return body;
    }
    public void setBody(Body body) {
        this.body = body;
    }
}
