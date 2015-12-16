package org.ovirt.engine.core.utils;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ovirt.engine.core.uutils.xml.SecureDocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtils {
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
}
