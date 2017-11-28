package org.ovirt.engine.core.utils.ovf.xml;

import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XmlTextWriter {

    private XMLStreamWriter writer;
    private StringWriter stream;

    public XmlTextWriter() {
        stream = new StringWriter();
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            writer = factory.createXMLStreamWriter(stream);
            writer.writeStartDocument("UTF-8", "1.0");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize xml writer: ", e);
        }
    }

    public void setPrefix(String prefix, String uri) {
        try {
            writer.setPrefix(prefix, uri);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to set prefix", e);
        }
    }

    public void writeNamespace(String prefix, String uri) {
        try {
            writer.writeNamespace(prefix, uri);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write namespace", e);
        }
    }

    public void writeDefaultNamespace(String uri) {
        try {
            writer.writeDefaultNamespace(uri);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write default namespace", e);
        }
    }

    public void writeStartDocument(boolean b) {
        // nothing, see ctor
    }

    public void writeStartElement(String namespaceURI, String localName) {
        try {
            writer.writeStartElement(namespaceURI, localName);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write start element", e);
        }
    }

    public void writeAttributeString(String namespaceURI, String localName, String value) {
        try {
            writer.writeAttribute(namespaceURI, localName, value);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write attribute", e);
        }
    }

    public void writeAttributeString(String prefix, String namespaceURI, String localName, String value) {
        try {
            writer.writeAttribute(prefix, namespaceURI, localName, value);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write attribute", e);
        }
    }

    public void writeAttributeString(String localName, String value) {
        try {
            writer.writeAttribute(localName, value);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write attribute", e);
        }
    }

    public void writeEndElement() {
        try {
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write end element", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void writeMap(Map<String, Object> map) {
        if (map != null) {
            for (Entry<String, Object> param : map.entrySet()) {
                writeStartElement(param.getKey());

                Object value = param.getValue();

                if (value instanceof String) {
                    writeRaw((String) value);
                } else if (value instanceof Map) {
                    writeMap((Map<String, Object>) value);
                }

                writeEndElement();
            }
        }
    }

    public void writeStartElement(String localName) {
        try {
            writer.writeStartElement(localName);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write start element", e);
        }
    }

    public void writeRaw (String string) {
        try {
            if (string == null) {
                string = "";
            }
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
        writeStartElement(name);
        writeRaw(content);
        writeEndElement();
    }

    public void writeElement(String namespace, String name, String content) {
        writeStartElement(namespace, name);
        writeRaw(content);
        writeEndElement();
    }

    /**
     * Write out an entire element without content.
     * @param name The name of the element.
     */
    public void writeElement(String name) {
        writeElement(name, null);
    }

    public String getStringXML() {
        try {
            writer.writeEndElement();
            writer.flush();
            writer.close();
            return stream.getBuffer().toString();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

}
