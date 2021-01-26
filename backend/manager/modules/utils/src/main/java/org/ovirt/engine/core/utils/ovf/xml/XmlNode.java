package org.ovirt.engine.core.utils.ovf.xml;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlNode {

    public String innerText;
    public XmlAttributeCollection attributes;
    protected Node node;

    public XmlNode(Node node) {
        this.node = node;
        this.innerText = node.getTextContent();
        attributes = new XmlAttributeCollection(node.getAttributes());

    }

    public XmlNode selectSingleNode(String string, XmlNamespaceManager _xmlns) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            xPath.setNamespaceContext(_xmlns);
            Object o = xPath.evaluate(string, node, XPathConstants.NODE);
            return o != null ? new XmlNode((Node) o) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public XmlNode selectSingleNode(String string) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            Object o = xPath.evaluate(string, node, XPathConstants.NODE);
            return o != null ? new XmlNode((Node) o) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public XmlNodeList selectNodes(String string) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            Object o = xPath.evaluate(string, node, XPathConstants.NODESET);
            return new XmlNodeList((NodeList) o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public XmlNodeList selectNodes(String string, XmlNamespaceManager xmlns) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            xPath.setNamespaceContext(xmlns);
            Object o = xPath.evaluate(string, node, XPathConstants.NODESET);
            return new XmlNodeList((NodeList) o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public NodeList getChildNodes() {
        return node.getChildNodes();
    }

    public XmlNode appendChild(Node child) {
        return new XmlNode(node.appendChild(child));
    }

}
