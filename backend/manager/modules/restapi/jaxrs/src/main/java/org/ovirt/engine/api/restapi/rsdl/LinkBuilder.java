package org.ovirt.engine.api.restapi.rsdl;

import org.ovirt.engine.api.model.Body;
import org.ovirt.engine.api.model.HttpMethod;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Request;
import org.ovirt.engine.api.model.Response;

public class LinkBuilder {
    private Link link = new Link();;
    public LinkBuilder url(String url) {
        link.setHref(url);
        return this;
    }
    public LinkBuilder rel(String rel) {
        link.setRel(rel);
        return this;
    }
    public LinkBuilder requestParameter(final String requestParameter) {
        link.setRequest(new Request());
        link.getRequest().setBody(new Body(){{setType(requestParameter);}});
        return this;
    }
    public LinkBuilder responseType(final String responseType) {
        link.setResponse(new Response(){{setType(responseType);}});
        return this;
    }
    public LinkBuilder httpMethod(HttpMethod httpMethod) {
        if(!link.isSetRequest()) {
            link.setRequest(new Request());
        }
        link.getRequest().setHttpMethod(httpMethod);
        return this;
    }
    public Link build() {
        return link;
    }
}

