package org.ovirt.engine.api.restapi.rsdl;

import org.ovirt.engine.api.model.Body;
import org.ovirt.engine.api.model.DetailedLink;
import org.ovirt.engine.api.model.HttpMethod;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Request;
import org.ovirt.engine.api.model.Response;

public class LinkBuilder {
    private DetailedLink detailedLink = new DetailedLink();
    public LinkBuilder url(String url) {
        detailedLink.setHref(url);
        return this;
    }
    public LinkBuilder rel(String rel) {
        detailedLink.setRel(rel);
        return this;
    }
    public LinkBuilder requestParameter(final String requestParameter) {
        Body body = new Body();
        body.setType(requestParameter);
        Request request = new Request();
        request.setBody(body);
        detailedLink.setRequest(request);
        return this;
    }
    public LinkBuilder responseType(final String responseType) {
        Response response = new Response();
        response.setType(responseType);
        detailedLink.setResponse(response);
        return this;
    }
    public LinkBuilder httpMethod(HttpMethod httpMethod) {
        if(!detailedLink.isSetRequest()) {
            detailedLink.setRequest(new Request());
        }
        detailedLink.getRequest().setHttpMethod(httpMethod);
        return this;
    }
    public Link build() {
        return detailedLink;
    }
}

