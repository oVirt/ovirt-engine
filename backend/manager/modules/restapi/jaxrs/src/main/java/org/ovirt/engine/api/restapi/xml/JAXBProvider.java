/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.ovirt.engine.api.model.Api;
import org.ovirt.engine.api.model.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for converting XML documents into model objects, and the other way around. Note that it
 * can't be a generic class because if it is then the JAX-RS framework will select other builtin classes that are more
 * specific.
 */
@Provider
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class JAXBProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    /**
     * The logger used by this class.
     */
    private static final Logger log = LoggerFactory.getLogger(JAXBProvider.class);

    /**
     * The package of the classes that this provider supports.
     */
    private static final Package typesPackage = Api.class.getPackage();

    /**
     * The factory used to create JAXB elements.
     */
    private ObjectFactory objectFactory = new ObjectFactory();

    /**
     * A index used to speed up finding the factory method used to create JAXB elements.
     */
    private Map<Class<?>, Method> factoryMethods = new HashMap<>();

    /**
     * The factory used to create XML document readers.
     */
    private XMLInputFactory parserFactory;

    /**
     * The JAXB jaxbContext used to convert XML documents into the corresponding model objects.
     */
    private JAXBContext jaxbContext;

    /**
     * Default event handler recognizes XML parsing as error and not as warning.
     */
    private ValidationEventHandler jaxbHandler = new JAXBValidationEventHandler();

    public JAXBProvider() {
        // In order to create the JAXB element that wraps the object we need to call the method of the object factory
        // that uses the correct element name, and in order to avoid doing this with every request we populate this
        // map in advance:
        for (Method factoryMethod : ObjectFactory.class.getDeclaredMethods()) {
            Class<?>[] parameterTypes = factoryMethod.getParameterTypes();
            if (parameterTypes.length == 1) {
                factoryMethods.put(parameterTypes[0], factoryMethod);
            }
        }

        // Create a factory that will produce XML parsers that ignore entity references and DTDs:
        parserFactory = XMLInputFactory.newFactory();
        parserFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        parserFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        parserFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        // Create a JAXB context for the tyeps package:
        try {
            jaxbContext = JAXBContext.newInstance(typesPackage.getName());
        } catch(JAXBException exception) {
            log.error("Can't create JAXB context for package \"{}\"", typesPackage.getName(), exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        XMLStreamReader reader = null;
        try {
            reader = parserFactory.createXMLStreamReader(entityStream, "UTF-8");
            return readFrom(reader);
        } catch(XMLStreamException exception) {
            throw new IOException(exception);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException exception) {
                    log.warn("Can't close XML stream reader.", exception);
                }
            }
        }
    }

    /**
     * Read the XML document using the given reader and convert it to an object. The given reader will be closed by the
     * caller.
     */
    private Object readFrom(XMLStreamReader reader) throws IOException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(jaxbHandler);
            Object result = unmarshaller.unmarshal(reader);
            if (result instanceof JAXBElement) {
                result = ((JAXBElement) result).getValue();
            }
            return result;
        } catch(JAXBException exception) {
            Throwable linked = exception.getLinkedException();
            if (linked != null) {
                Throwable cause = linked;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                throw new IOException(cause);
            }
            throw new IOException(exception);
        }
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        // Find the factory method used to create the JAXB element with the right tag:
        Method factoryMethod = factoryMethods.get(type);
        if (factoryMethod == null) {
            throw new IOException("Can't find factory method for type \"" + type.getName() + "\".");
        }

        // Invoke the method to create the JAXB element:
        JAXBElement<Object> element;
        try {
            element = (JAXBElement<Object>) factoryMethod.invoke(objectFactory, object);
        } catch(IllegalAccessException|InvocationTargetException exception) {
            throw new IOException("Error invoking factory method for type \"" +  type.getName() + "\".", exception);
        }

        // Marshal the element:
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(element, entityStream);
        } catch(JAXBException exception) {
            throw new IOException("Can't marshall JAXB element of type \"" + type.getName() + "\".", exception);
        }
    }
}
