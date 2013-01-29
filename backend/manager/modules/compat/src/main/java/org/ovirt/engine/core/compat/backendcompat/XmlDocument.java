package org.ovirt.engine.core.compat.backendcompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ovirt.engine.core.compat.CompatException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlDocument {

    public Object NameTable;
    public String OuterXml;
    public XmlNode[] ChildNodes;

    private Document doc;

    public void LoadXml(String ovfstring) {
        try {
            // load doc
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            fact.setNamespaceAware(true);
            DocumentBuilder builder = fact.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(ovfstring)));

            // initialize all the child nodes
            NodeList list = doc.getElementsByTagName("*");
            ChildNodes = new XmlNode[list.getLength()];
            for (int i = 0; i < list.getLength(); i++) {
                ChildNodes[i] = new XmlNode(list.item(i));
            }

            OuterXml = ovfstring;
        } catch (Exception e) {
            CompatException ce = new CompatException(e.getMessage(), e.getCause());
            throw ce;
        }
    }

    public XmlNode SelectSingleNode(String string) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            Object o = xPath.evaluate(string, doc, XPathConstants.NODE);
            return new XmlNode((Node) o);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public XmlNode SelectSingleNode(String string, XmlNamespaceManager _xmlns) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            xPath.setNamespaceContext(_xmlns);
            Object o = xPath.evaluate(string, doc, XPathConstants.NODE);
            return new XmlNode((Node) o);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public XmlNodeList SelectNodes(String string) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            Object o = xPath.evaluate(string, doc, XPathConstants.NODESET);
            return new XmlNodeList((NodeList) o);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public XmlNodeList SelectNodes(String string, XmlNamespaceManager _xmlns) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            xPath.setNamespaceContext(_xmlns);
            Object o = xPath.evaluate(string, doc, XPathConstants.NODESET);
            return new XmlNodeList((NodeList) o);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to evaluate xpath: " + string, e);
        }
    }

    public void Load(String filename) {
        InputStream is = null;
        try {
            is = new FileInputStream(filename);
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer buffer = new StringBuffer();

            String line;
            while (null != (line = r.readLine())) {
                buffer.append(line);
                buffer.append("\n");
            }

            LoadXml(buffer.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filename, e);
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }
}
