package org.ovirt.engine.api.restapi.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.ovirt.engine.api.model.ObjectFactory;
import org.ovirt.engine.api.model.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

@Provider
@Produces("application/pdf")
public class FOPMessageBodyWriter implements MessageBodyWriter<Object> {

    private static final Logger log = LoggerFactory.getLogger(FOPMessageBodyWriter.class);
    private JAXBContext jaxbContext;
    private TransformerFactory transfact;
    private FopFactory fopFactory;
    private FOUserAgent foUserAgent;
    private ObjectFactory objectFactory;

    public FOPMessageBodyWriter() {
        try {
            String modelPackage = API.class.getPackage().getName();
            jaxbContext = JAXBContext.newInstance(modelPackage);
            transfact = TransformerFactory.newInstance();
            fopFactory = FopFactory.newInstance();
            foUserAgent = fopFactory.newFOUserAgent();
            objectFactory = new ObjectFactory();
        } catch (Exception error) {
            log.error("Error while creating FOP message body writer.", error);
        }
    }

    @Override
    public long getSize(Object data, Class<?> dataClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> dataClass, Type arg1, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(final Object data, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        String xslName = "/" + type.getSimpleName() + "AsPdf.xsl";
        try (InputStream templateStream = type.getResourceAsStream(xslName)) {
            if (templateStream != null) {
                StreamSource transformSource = new StreamSource(templateStream);

                Method factoryMethod = null;
                for (Method currentMethod : objectFactory.getClass().getDeclaredMethods()) {
                    Class<?>[] parameterTypes = currentMethod.getParameterTypes();
                    if (parameterTypes.length == 1 && parameterTypes[0] == type) {
                        factoryMethod = currentMethod;
                        break;
                    }
                }
                if (data != null && factoryMethod != null) {
                    JAXBElement<?> element = (JAXBElement<?>) factoryMethod.invoke(objectFactory, data);
                    Source source = new JAXBSource(jaxbContext, element);

                    Transformer xslfoTransformer = transfact.newTransformer(transformSource);
                    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, entityStream);
                    Result res = new SAXResult(fop.getDefaultHandler());
                    xslfoTransformer.transform(source, res);
                } else {
                    log.error("Data not available");
                }
            } else {
                log.error("Error while generating PDF. Null InputStream");
            }
        } catch (Exception e) {
            log.error("Error while generating PDF. ", e);
        }
    }
}
