package org.ovirt.engine.core.compat.backendcompat;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.ovirt.engine.core.compat.Encoding;

import java.io.FileOutputStream;

public class XmlTextWriter {

    public Object Formatting;
    public int Indentation;

    XMLStreamWriter writer;

    public XmlTextWriter(String name, Encoding utf8) {
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            writer = factory.createXMLStreamWriter(new FileOutputStream(name));
            writer.writeStartDocument("UTF-8", "1.0");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize xml writer: " + name, e);
        }
    }

    public void WriteStartDocument(boolean b) {
        // nothing, see ctor
    }

    public void WriteStartElement(String prefix, String localName, String namespaceURI) {
        try {
            writer.writeStartElement(prefix, localName, namespaceURI);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write start element", e);
        }
    }

    public void WriteAttributeString(String prefix, String localName, String namespaceURI, String value) {
        try {
            writer.writeAttribute(prefix, namespaceURI, localName, value);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write attribute", e);
        }
    }

    public void WriteEndElement() {
        try {
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write end element", e);
        }
    }

    public void WriteStartElement(String localName) {
        try {
            writer.writeStartElement(localName);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write start element", e);
        }
    }

    public void WriteRaw(String string) {
        try {
            if (string == null)
                string = "";
            writer.writeCharacters(string);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write CDATA", e);
        }
    }

    public void close() {
        try {
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to close xml writer", e);
        }
    }

    public void WriteAttributeString(String prefix, String localName, String namespaceURI, int value) {
        WriteAttributeString(prefix, namespaceURI, localName, Integer.toString(value));
    }

}
