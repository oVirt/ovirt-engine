/*
Copyright (c) 2014-2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
import org.ovirt.engine.api.restapi.invocation.VersionSource;
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
        StringBuilder buffer = new StringBuilder();
        buffer.append(current.getPrefix());
        if (current.getVersionSource() == VersionSource.URL) {
            buffer.append("/v");
            buffer.append(current.getVersion());
        }
        String prefix = buffer.toString();

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
                }
                else {
                    href = prefix + "/" + href;
                }
                node.setNodeValue(href);
            }
        }
        catch (Exception exception) {
            throw new IOException(exception);
        }

        // Create the RSDL object:
        return JAXB.unmarshal(new DOMSource(document), clazz);
    }
}
