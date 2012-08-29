package org.ovirt.engine.core.utils;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtils {
    /**
     * Gets the node Attribute value using XPath.
     *
     * @param doc
     *            The doc.
     * @param xPath
     *            The x path pattern to the node.
     * @param attribute
     *            The attribute name.
     * @param error
     *            The error string (if occured).
     * @return
     */
    // public static String GetNodeAttributeValue(XmlDocument doc, String xPath,
    // String attribute, RefObject<String> error)
    // {
    // throw new NotImplementedException() ;
    // String value = "";
    // error.argvalue = "";
    //
    // try
    // {
    // XmlNode node = doc.SelectSingleNode(xPath);
    // if (node != null)
    // {
    // value = node.Attributes[attribute].getValue();
    // }
    // }
    // catch (RuntimeException ex)
    // {
    // error.argvalue = ex.getMessage();
    // }
    // return value;
    // }

    /**
     * Sets the node Attribute value using XPath.
     *
     * @param doc
     *            The doc.
     * @param xPath
     *            The x path pattern to the node.
     * @param attribute
     *            The attribute name.
     * @param value
     *            The value.
     * @param error
     *            The error string (if occured).
     */
    // public static void SetNodeAttributeValue(XmlDocument doc, String xPath,
    // String attribute, String value, RefObject<String> error)
    // {
    // throw new NotImplementedException() ;
    // error.argvalue = "";
    // try
    // {
    // XmlNode node = doc.SelectSingleNode(xPath);
    // if (node != null)
    // {
    // node.Attributes[attribute].setValue(value);
    // }
    // }
    // catch (RuntimeException ex)
    // {
    // String.format("Unable to update certificate finger print in %1$s/[%2$s\n]",
    // xPath, attribute);
    // error.argvalue += ex.getMessage();
    // }
    // }

    /**
     * Load the xml document using the xml string
     * @param xmlString
     *            The xml string value
     * @return Document Return the loaded Document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document loadXmlDoc(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlString));
        Document doc = docBuilder.parse(is);
        // normalize text representation
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Get the int value
     * @param element
     * @param tagName
     * @return int value
     */
    public static int getIntValue(Element element, String tagName) {
        return Integer.parseInt(getTextValue(element, tagName));
    }

    /**
     * Get the Text value
     * @param element
     * @param tagName
     * @return String value
     */
    public static String getTextValue(Element element, String tagName) {
        return element.getElementsByTagName(tagName).item(0).getChildNodes().item(0).getNodeValue().trim();
    }
}
