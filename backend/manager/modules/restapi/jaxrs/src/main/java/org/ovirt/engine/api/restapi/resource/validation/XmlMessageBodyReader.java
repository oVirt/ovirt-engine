package org.ovirt.engine.api.restapi.resource.validation;

import org.jboss.resteasy.plugins.providers.jaxb.AbstractJAXBProvider;
import org.jboss.resteasy.plugins.providers.jaxb.JAXBMarshalException;
import org.ovirt.engine.api.utils.InvalidValueException;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes({ MediaType.APPLICATION_XML })
public class XmlMessageBodyReader extends AbstractJAXBProvider<Object> {

    /** Default event handler recognizes XML parsing as error and not as warning */
    private ValidationEventHandler errorhandler = new DefaultValidationEventHandler();

    @Override
    protected boolean isReadWritable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Standard JAXB unmarshaller with custom error handler- {@link #errorhandler}
     */
    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

        if (entityStream == null) {
            return null;
        }
        try {
            JAXBContext ctx = findJAXBContext(type, annotations, mediaType, true);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();

            AbstractJAXBProvider.decorateUnmarshaller(type, annotations, mediaType, unmarshaller);

            unmarshaller.setEventHandler(errorhandler);
            Object parsedObj = unmarshaller.unmarshal(entityStream);
            if (parsedObj instanceof JAXBElement) {
                return ((JAXBElement) parsedObj).getValue();
            }
            return parsedObj;
        }
        catch (JAXBException exception) {
            Throwable linked = exception.getLinkedException();
            if (linked != null) {
                Throwable cause = linked.getCause();
                if (cause instanceof InvalidValueException) {
                    throw (InvalidValueException) cause;
                }
            }
            throw new JAXBMarshalException(exception);
        }
    }

}
