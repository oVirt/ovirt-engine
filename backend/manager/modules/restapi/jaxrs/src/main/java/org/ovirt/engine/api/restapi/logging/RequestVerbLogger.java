package org.ovirt.engine.api.restapi.logging;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.WebApplicationException;

import org.jboss.resteasy.annotations.interception.DecoderPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

@Provider
@ServerInterceptor
@DecoderPrecedence
public class RequestVerbLogger extends MessageLogger implements PreProcessInterceptor {

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
        if (LOG.isDebugEnabled()) {
            logVerb(request);
        }
        return null;
    }

    protected void logVerb(HttpRequest request) {
        LOG.debug(request.getHttpMethod() + " " + request.getUri().getRequestUri());
    }
}
