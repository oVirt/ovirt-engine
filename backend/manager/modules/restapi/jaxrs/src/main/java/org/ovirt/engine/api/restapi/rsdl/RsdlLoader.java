/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.rsdl;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.rsdl.RsdlIOManager;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.uutils.xml.SecureDocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains a method used to load an RSDL document and convert it to the corresponding Java object.
 */
public class RsdlLoader {
    /**
     * Loads the RSDL object appropriate for the current request and converts it to the corresponding Java object. Note
     * that the RSDL object will be of different classes in V3 and V4 of the API, that is why the class is passed as
     * a parameter, to be able to use this method for both V3 and V4.
     *
     * @param clazz the class of the RSDL object
     * @param <RSDL> the type of the RSDL object
     */
    public static <RSDL> RSDL loadRsdl(Class<RSDL> clazz) throws IOException {
        // Decide what version of the RSDL document to load:
        Current current = CurrentManager.get();
        String fileName = current.getApplicationMode() == ApplicationMode.GlusterOnly? "rsdl_gluster.xml": "rsdl.xml";
        String resourcePath = String.format("/v%s/%s", current.getVersion(), fileName);

        // Calculate the prefix that will be used in the "href" attributes:
        String prefix = current.getAbsolutePath();

        // Load the RSDL document into a DOM tree and then modify all the "href" attributes to include the prefix that
        // has been previously calculated:
        Document document;
        try {
            DocumentBuilder parser = SecureDocumentBuilderFactory.newDocumentBuilderFactory().newDocumentBuilder();
            try (InputStream in = RsdlIOManager.class.getResourceAsStream(resourcePath)) {
                document = parser.parse(in);
            }
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate("//@href", document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String href = node.getNodeValue();
                if (href.startsWith("?")) {
                    href = prefix + href;
                } else {
                    href = prefix + "/" + href;
                }
                node.setNodeValue(href);
            }
        } catch(Exception exception) {
            throw new IOException(exception);
        }

        // Create the RSDL object:
        return JAXB.unmarshal(new DOMSource(document), clazz);
    }
}
