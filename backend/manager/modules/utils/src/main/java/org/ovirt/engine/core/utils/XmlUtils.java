package org.ovirt.engine.core.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ovirt.engine.core.uutils.xml.SecureDocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtils {
    private static final Logger log = LoggerFactory.getLogger(XmlUtils.class);

    /**
     * Load the xml document using the xml string
     * @param xmlString
     *            The xml string value
     * @return Document Return the loaded Document
     */
    public static Document loadXmlDoc(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = SecureDocumentBuilderFactory.newDocumentBuilderFactory();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlString));
        Document doc = docBuilder.parse(is);
        // normalize text representation
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Get the int value
     * @return int value
     */
    public static int getIntValue(Element element, String tagName) {
        return Integer.parseInt(getTextValue(element, tagName));
    }

    /**
     * Get the Text value
     * @return String value
     */
    public static String getTextValue(Element element, String tagName) {
        return element.getElementsByTagName(tagName).item(0).getChildNodes().item(0).getNodeValue().trim();
    }

    public static String prettify(String input) {
        Source xmlInput = new StreamSource(new StringReader(input));
        StringWriter stringWriter = new StringWriter();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, new StreamResult(stringWriter));
            return stringWriter.toString().replace("\r\n", "\n");
        } catch (Exception ex) {
            log.error("Failed to produce pretty-print of {}", input);
            log.error("Exception:", ex);
            return null;
        }
    }
}
