package org.ovirt.engine.api.restapi.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.WebApplicationException;

import org.jboss.resteasy.annotations.interception.DecoderPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyReaderContext;
import org.jboss.resteasy.spi.interception.MessageBodyReaderInterceptor;

@Provider
@ServerInterceptor
@DecoderPrecedence
public class RequestPayloadLogger extends MessageLogger implements MessageBodyReaderInterceptor {

    @Override
    public Object read(MessageBodyReaderContext context) throws IOException, WebApplicationException {
        InputStream old = context.getInputStream();
        if (LOG.isDebugEnabled()) {
            logHeaders(context);
            context.setInputStream(new LoggingInputStream(old, LOG));
        }
        try {
            return context.proceed();
        } finally {
            context.setInputStream(old);
        }
    }

    protected void logHeaders(MessageBodyReaderContext context) {
        for (Entry<String, List<String>> entry : context.getHeaders().entrySet()) {
            StringBuilder header = new StringBuilder(entry.getKey());
            header.append(":");
            for (String value : entry.getValue()) {
                header.append(value).append(" ");
            }
            LOG.debug(header);
        }
    }
}
