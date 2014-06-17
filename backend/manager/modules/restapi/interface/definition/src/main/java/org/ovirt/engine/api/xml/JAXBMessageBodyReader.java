/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.ovirt.engine.api.model.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for converting XML documents into model objects. Note that it can't be a generic class
 * because if it is then the JAX-RS framework will select other builtin classes that are more specific.
 */
@Provider
@Consumes(MediaType.APPLICATION_XML)
public class JAXBMessageBodyReader implements MessageBodyReader<Object> {
    /**
     * The logger used by this class.
     */
    private Logger log = LoggerFactory.getLogger(JAXBMessageBodyReader.class);

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

    public JAXBMessageBodyReader() {
        // Create a factory that will produce XML parsers that ignore entity references and DTDs:
        parserFactory = XMLInputFactory.newFactory();
        parserFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        parserFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        parserFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        // Create a JAXB context for the model package:
        String modelPackage = API.class.getPackage().getName();
        try {
            jaxbContext = JAXBContext.newInstance(modelPackage);
        }
        catch (JAXBException exception) {
            log.error("Can't create JAXB context for package \"{}\".", modelPackage, exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation annotations[], MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        XMLStreamReader reader = null;
        try {
            reader = parserFactory.createXMLStreamReader(entityStream, "UTF-8");
            return readFrom(reader);
        }
        catch (XMLStreamException exception) {
            throw new IOException(exception);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (XMLStreamException exception) {
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
        }
        catch (JAXBException exception) {
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
}
