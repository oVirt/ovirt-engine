package org.ovirt.engine.api.restapi.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.WebApplicationException;

import org.jboss.resteasy.annotations.interception.DecoderPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyWriterContext;
import org.jboss.resteasy.spi.interception.MessageBodyWriterInterceptor;

@Provider
@ServerInterceptor
@DecoderPrecedence
public class ResponsePayloadLogger extends MessageLogger implements MessageBodyWriterInterceptor {

    @Override
    public void write(MessageBodyWriterContext context) throws IOException, WebApplicationException {
        OutputStream old = context.getOutputStream(), wrapper = null;
        if (LOG.isDebugEnabled()) {
            logHeaders(context);
            wrapper = new LoggingOutputStream(old, LOG);
            context.setOutputStream(wrapper);
        }

        try {
            context.proceed();
        } finally {
            if (wrapper != null) {
                try {
                    wrapper.close();
                } catch (IOException ioe) {
                    // never thrown
                }
                context.setOutputStream(old);
            }
        }
    }

    protected void logHeaders(MessageBodyWriterContext context) {
        for (Map.Entry<String, List<Object>> entry : context.getHeaders().entrySet()) {
            StringBuilder header = new StringBuilder(entry.getKey());
            header.append(":");
            for (Object value : entry.getValue()) {
                header.append(value).append(" ");
            }
            LOG.debug(header);
        }
    }

}
