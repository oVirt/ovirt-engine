package org.ovirt.engine.core.compat.backendcompat;

import java.io.FileOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.ovirt.engine.core.compat.Encoding;

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

    public void WriteMap(Map<String,Object> map) {
        if (map != null) {
            for (Entry<String,Object> param : map.entrySet()) {
                WriteStartElement(param.getKey());

                Object value = param.getValue();

                if (value instanceof String) {
                    WriteRaw((String) value);
                } else if (value instanceof Map) {
                    WriteMap((Map<String,Object>) value);
                }

                WriteEndElement();
            }
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

    /**
     * Write out an entire element in a simple format:
     *
     * <pre>
     * &lt;name>content&lt;/name>
     * </pre>
     *
     * @param name
     *            The name of the element.
     * @param content
     *            The content to write inside the element.
     */
    public void writeElement(String name, String content) {
        WriteStartElement(name);
        WriteRaw(content);
        WriteEndElement();
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
