package org.ovirt.engine.core.utils.ovf.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

public class XmlNode {

    public String innerText;
    public XmlAttributeCollection attributes;
    protected Node node;

    public XmlNode(Node node) {
        this.node = node;
        this.innerText = node.getTextContent();
        attributes = new XmlAttributeCollection(node.getAttributes());

    }

    public XmlNode SelectSingleNode(String string, XmlNamespaceManager _xmlns) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            xPath.setNamespaceContext(_xmlns);
            Object o = xPath.evaluate(string, node, XPathConstants.NODE);
            if (o != null) {
                return new XmlNode((Node) o);
            }
            return null;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public XmlNode SelectSingleNode(String string) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            Object o = xPath.evaluate(string, node, XPathConstants.NODE);
            if (o != null) {
                return new XmlNode((Node) o);
            }
            return null;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public XmlNodeList SelectNodes(String string) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            Object o = xPath.evaluate(string, node, XPathConstants.NODESET);
            return new XmlNodeList((NodeList) o);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public NodeList GetChildNodes() {
        return node.getChildNodes();
    }
}
