package org.ovirt.engine.api.restapi.logging;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.DecoderPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

@Provider
@ServerInterceptor
@DecoderPrecedence
public class ResponseStatusLogger extends MessageLogger implements PostProcessInterceptor {

    @Override
    public void postProcess(ServerResponse response) {
        if (LOG.isDebugEnabled()) {
            logStatus(response);
        }
    }

    protected void logStatus(ServerResponse response) {
        LOG.debug(response.getStatus() + " " + Response.Status.fromStatusCode(response.getStatus()));
    }
}

