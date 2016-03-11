/*
Copyright (c) 2016 Red Hat, Inc.

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

package org.ovirt.engine.api.v3.servers;

import java.io.InputStream;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.invocation.VersionSource;
import org.ovirt.engine.api.v3.types.V3Capabilities;
import org.ovirt.engine.api.v3.types.V3VersionCaps;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Produces({"application/xml", "application/json"})
public class V3CapabilitiesServer {
    @GET
    public V3Capabilities list() {
        // Calculate the prefix that should be added to the "href" attributes:
        Current current = CurrentManager.get();
        StringBuilder buffer = new StringBuilder();
        if (current.getVersionSource() == VersionSource.URL) {
            buffer.append("/v");
            buffer.append(current.getVersion());
        }
        buffer.append(current.getPrefix());
        String prefix = buffer.toString();

        // Load the document into a DOM tree:
        Document document;
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            try (InputStream in = this.getClass().getResourceAsStream("/v3/capabilities.xml")) {
                document = parser.parse(in);
            }
        }
        catch (Exception exception) {
            throw new WebApplicationException(exception, Response.Status.INTERNAL_SERVER_ERROR);
        }

        // Create an XPath engine, we will use it for several things later:
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            // Find the 3.6 capabilities and duplicate them for 4.0, as from the point of view of the user of version 3
            // of the API version 4.0 should be identical to version 3.6:
            Element versionElement = (Element) xpath.evaluate("/capabilities/version[@major='3' and @minor='6']",
                document, XPathConstants.NODE);
            versionElement = (Element) versionElement.cloneNode(true);
            String versionId = "332e4033-2e40-332e-4033-2e40332e4033";
            versionElement.setAttribute("id", versionId);
            versionElement.setAttribute("href", "/capabilities/" + versionId);
            versionElement.setAttribute("major", "4");
            versionElement.setAttribute("minor", "0");

            // Set the "current" flag of the 4.0 capabilities to "true":
            Element currentElement = (Element) xpath.evaluate("current", versionElement, XPathConstants.NODE);
            currentElement.setTextContent("true");

            // Add the 4.0 capabilities to the end of the document:
            document.getDocumentElement().appendChild(versionElement);
        }
        catch (XPathExpressionException exception) {
            throw new WebApplicationException(exception, Response.Status.INTERNAL_SERVER_ERROR);
        }

        // Modify all the "href" attributes to include the prefix:
        try {
            NodeList nodes = (NodeList) xpath.evaluate("//@href", document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String href = node.getNodeValue();
                href = prefix + href;
                node.setNodeValue(href);
            }
        }
        catch (XPathExpressionException exception) {
            throw new WebApplicationException(exception, Response.Status.INTERNAL_SERVER_ERROR);
        }

        // Create the capabilities object from the DOM tree:
        return JAXB.unmarshal(new DOMSource(document), V3Capabilities.class);
    }

    @GET
    @Path("/{id}")
    public V3VersionCaps get(@PathParam("id") String id) {
        V3Capabilities capabilities = list();
        Optional<V3VersionCaps> caps = capabilities.getVersions().stream()
            .filter(x -> x.getId().equals(id))
            .findFirst();
        if (caps.isPresent()) {
            return caps.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
