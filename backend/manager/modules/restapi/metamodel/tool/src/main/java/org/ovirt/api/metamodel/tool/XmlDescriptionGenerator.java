/*
Copyright (c) 2015 Red Hat, Inc.

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

package org.ovirt.api.metamodel.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import org.ovirt.api.metamodel.concepts.Attribute;
import org.ovirt.api.metamodel.concepts.Concept;
import org.ovirt.api.metamodel.concepts.EnumType;
import org.ovirt.api.metamodel.concepts.EnumValue;
import org.ovirt.api.metamodel.concepts.Link;
import org.ovirt.api.metamodel.concepts.ListType;
import org.ovirt.api.metamodel.concepts.Locator;
import org.ovirt.api.metamodel.concepts.Method;
import org.ovirt.api.metamodel.concepts.Model;
import org.ovirt.api.metamodel.concepts.Name;
import org.ovirt.api.metamodel.concepts.Parameter;
import org.ovirt.api.metamodel.concepts.PrimitiveType;
import org.ovirt.api.metamodel.concepts.Service;
import org.ovirt.api.metamodel.concepts.StructType;
import org.ovirt.api.metamodel.concepts.Type;

/**
 * This class takes a model and generates its XML description.
*/
@ApplicationScoped
public class XmlDescriptionGenerator {
    private Model model;
    private XMLStreamWriter writer;

    public void generate(Model model, File file) {
        // Save the model:
        this.model = model;

        // Create the XML writer:
        try (OutputStream out = new FileOutputStream(file)) {
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            writer = factory.createXMLStreamWriter(out, "UTF-8");
            IndentingXMLStreamWriter indenter = new IndentingXMLStreamWriter(writer);
            indenter.setIndentStep("  ");
            writer = indenter;
            writeModel();
            writer.close();
        }
        catch (IOException | XMLStreamException exception) {
            throw new IllegalStateException("Can't generate XML representation", exception);
        }
    }

    private void writeModel() {
        writeStartElement("model");
        writeStartElement("types");
        model.types().forEach(this::writeType);
        writeEndElement();
        writeStartElement("services");
        model.services().forEach(this::writeService);
        writeEndElement();
        writeEndElement();
    }

    private void writeType(Type type) {
        if (type instanceof PrimitiveType) {
            writePrimitiveType((PrimitiveType) type);
        }
        else if (type instanceof StructType) {
            writeStructType((StructType) type);
        }
        else if (type instanceof EnumType) {
            writeEnumType((EnumType) type);
        }
    }

    private void writePrimitiveType(PrimitiveType type) {
        writeStartElement("primitive");
        writeCommon(type);
        writeEndElement();
    }

    private void writeStructType(StructType type) {
        writeStartElement("struct");
        writeCommon(type);
        type.attributes().forEach(this::writeAttribute);
        type.links().forEach(this::writeStructLink);
        writeEndElement();
    }

    private void writeAttribute(Attribute attribute) {
        writeStartElement("attribute");
        writeCommon(attribute);
        writeTypeRef(attribute.getType());
        writeEndElement();
    }

    private void writeStructLink(Link link) {
        writeStartElement("link");
        writeCommon(link);
        writeTypeRef(link.getType());
        writeEndElement();
    }

    private void writeEnumType(EnumType type) {
        writeStartElement("enum");
        writeCommon(type);
        type.values().forEach(this::writeEnumValue);
        writeEndElement();
    }

    private void writeEnumValue(EnumValue value) {
        writeStartElement("value");
        writeCommon(value);
        writeEndElement();
    }

    private void writeService(Service service) {
        writeStartElement("service");
        writeCommon(service);
        service.methods().forEach(this::writeServiceMethod);
        service.locators().forEach(this::writeServiceLocator);
        writeEndElement();
    }

    private void writeServiceMethod(Method method) {
        writeStartElement("method");
        writeCommon(method);
        method.parameters().forEach(this::writeParameter);
        writeEndElement();
    }

    private void writeServiceLocator(Locator locator) {
        writeStartElement("locator");
        writeCommon(locator);
        locator.parameters().forEach(this::writeParameter);
        writeEndElement();
    }

    private void writeParameter(Parameter parameter) {
        writeStartElement("parameter");
        writeCommon(parameter);
        writeStartElement("in");
        writeCharacters(Boolean.toString(parameter.isIn()));
        writeEndElement();
        writeStartElement("out");
        writeCharacters(Boolean.toString(parameter.isIn()));
        writeEndElement();
        writeTypeRef(parameter.getType());
        writeEndElement();
    }

    private void writeCommon(Concept concept) {
        writeDoc(concept);
        writeName(concept);
    }

    private void writeName(Concept concept) {
        Name name = concept.getName();
        if (name != null) {
            writeStartElement("name");
            writeCharacters(name.toString());
            writeEndElement();
        }
    }

    private void writeDoc(Concept concept) {
        String doc = concept.getDoc();
        if (doc != null) {
            writeStartElement("doc");
            writeCData(doc);
            writeEndElement();
        }
    }

    private void writeTypeRef(Type type) {
        writeStartElement("type");
        writeCharacters(getTypeRef(type));
        writeEndElement();
    }

    private String getTypeRef(Type type) {
        if (type instanceof StructType || type instanceof PrimitiveType) {
            return type.getName().toString();
        }
        if (type instanceof ListType) {
            ListType listType = (ListType) type;
            Type elementType = listType.getElementType();
            return getTypeRef(elementType) + "[]";
        }
        return "";
    }

    private void writeStartElement(String tag) {
        try {
            writer.writeStartElement(tag);
        }
        catch (XMLStreamException exception) {
            throw new IllegalStateException("Can't write start element for tag \"" + tag + "\"", exception);
        }
    }

    private void writeEndElement() {
        try {
            writer.writeEndElement();
        }
        catch (XMLStreamException exception) {
            throw new IllegalStateException("Can't write end element", exception);
        }
    }

    private void writeCharacters(String text) {
        try {
            writer.writeCharacters(text);
        }
        catch (XMLStreamException exception) {
            throw new IllegalStateException("Can't write text \"" + text + "\"", exception);
        }
    }

    private void writeCData(String text) {
        try {
            writer.writeCData(text);
        }
        catch (XMLStreamException exception) {
            throw new IllegalStateException("Can't write text \"" + text + "\"", exception);
        }
    }
}

