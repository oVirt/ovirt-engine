package org.ovirt.engine.api.restapi.rsdl;

public class Request {

    private Body body;
    private Url url;
    public Body getBody() {
        return body;
    }
    public void setBody(Body body) {
        this.body = body;
    }
    public Url getUrl() {
        return url;
    }
    public void setUrl(Url url) {
        this.url = url;
    }
}
