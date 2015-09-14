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

package org.ovirt.engine.api.restapi.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.ovirt.engine.api.model.Api;
import org.ovirt.engine.api.model.ObjectFactory;
import org.ovirt.engine.api.resource.ApiMediaType;

/**
 * This writer generates PDF documents from model objects transforming them first into XML, then into FO (Formatting
 * Objects) using an XSLT transformation, and then into actual PDF using FOP.
 */
@Provider
@Produces(ApiMediaType.APPLICATION_PDF)
public class FOPMessageBodyWriter implements MessageBodyWriter<Object> {
    /**
     * The factory used to create JAXB elements.
     */
    private ObjectFactory objectFactory = new ObjectFactory();

    /**
     * A index used to speed up finding the factory method used to create JAXB elements.
     */
    private Map<Class<?>, Method> factoryMethods = new HashMap<>();

    /**
     * The JAXB context used to convert XML documents into the corresponding model objects.
     */
    private JAXBContext jaxbContext;

    /**
     * The factory used to create XSLT transformers.
     */
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();

    /**
     * The factory used to create FOP objects.
     */
    private FopFactory fopFactory;

    /**
     * The FO user agent.
     */
    private FOUserAgent foUserAgent;

    public FOPMessageBodyWriter() {
        // In order to create the JAXB element that wraps the object we need to call the method of the object factory
        // that uses the correct element name, and in order to avoid doing this with every request we populate this
        // map in advance:
        for (Method factoryMethod : ObjectFactory.class.getDeclaredMethods()) {
            Class<?>[] parameterTypes = factoryMethod.getParameterTypes();
            if (parameterTypes.length == 1) {
                factoryMethods.put(parameterTypes[0], factoryMethod);
            }
        }

        // Create a JAXB context for the model package:
        String modelPackage = Api.class.getPackage().getName();
        try {
            jaxbContext = JAXBContext.newInstance(modelPackage);
        }
        catch (JAXBException exception) {
            throw new IllegalStateException(
                "Can't create JAXB context for package \"" + modelPackage + "\".",
                exception
            );
        }

        // Create the XSLT transformer factory. Note that we need to explicitly use Xalan to avoid an unresolved issue
        // with WildFly:
        //
        //   Xalan Linkage error : TransformerConfigurationException
        //   https://issues.jboss.org/browse/WFCORE-519
        //
        // If/when this issue is solved this code can be changed to use the default implementation, calling the
        // "newInstance" method without parameters.
        transformerFactory = TransformerFactory.newInstance(
            "org.apache.xalan.processor.TransformerFactoryImpl",
            FOPMessageBodyWriter.class.getClassLoader()
        );

        // Create the FOP factory:
        fopFactory = FopFactory.newInstance();

        // Create the FO user agent:
        foUserAgent = fopFactory.newFOUserAgent();
    }

    @Override
    public long getSize(Object data, Class<?> dataClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> dataClass, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        // Locate and load the XSLT template:
        String templateName = "/pdf/" + type.getSimpleName() + ".xsl";
        Transformer template;
        try (InputStream templateStream = FOPMessageBodyWriter.class.getResourceAsStream(templateName)) {
            if (templateStream != null) {
                StreamSource templateSource = new StreamSource(templateStream);
                template = transformerFactory.newTransformer(templateSource);
            }
            else {
                throw new IOException("Can't find resource for XSLT template \"" + templateName + "\".");
            }
        }
        catch (TransformerConfigurationException exception) {
            throw new IOException("Can't load XSLT template \"" + templateName + "\".", exception);
        }

        // Find the factory method used to create the JAXB element with the right tag:
        Method factoryMethod = factoryMethods.get(type);
        if (factoryMethod == null) {
            throw new IOException("Can't find factory method for type \"" + type.getName() + "\".");
        }

        // Invoke the method to create the JAXB element:
        JAXBElement<Object> element;
        try {
            element = (JAXBElement<Object>) factoryMethod.invoke(objectFactory, object);
        }
        catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IOException("Error invoking factory method for type \"" + type.getName() + "\".", exception);
        }

        // Wrap the created JAXB element with an object that can be used as the source of the XSLT transformation:
        Source source;
        try {
            source = new JAXBSource(jaxbContext, element);
        }
        catch (JAXBException exception) {
            throw new IOException("Can't create transformation source from JAXB element.", exception);
        }

        // Run the XSLT transformation using the JAXB element as source and the entity stream as result:
        try {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, entityStream);
            Result result = new SAXResult(fop.getDefaultHandler());
            template.transform(source, result);
        }
        catch (TransformerException | FOPException exception) {
            throw new IOException(
                "Error while generating PDF document using XSLT template \"" + templateName + "\".",
                exception
            );
        }
    }
}
