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
import javax.enterprise.context.ApplicationScoped;

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
    private XmlWriter writer;

    public void generate(Model model, File file) {
        // Save the model:
        this.model = model;

        // Create the XML writer:
        try (XmlWriter tmp = new XmlWriter(file)) {
            writer = tmp;
            writeModel();
        }
    }

    private void writeModel() {
        writer.writeStartElement("model");
        writer.writeStartElement("types");
        model.types().forEach(this::writeType);
        writer.writeEndElement();
        writer.writeStartElement("services");
        model.services().forEach(this::writeService);
        writer.writeEndElement();
        writer.writeEndElement();
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
        writer.writeStartElement("primitive");
        writeCommon(type);
        writer.writeEndElement();
    }

    private void writeStructType(StructType type) {
        writer.writeStartElement("struct");
        writeCommon(type);
        type.attributes().forEach(this::writeAttribute);
        type.links().forEach(this::writeStructLink);
        writer.writeEndElement();
    }

    private void writeAttribute(Attribute attribute) {
        writer.writeStartElement("attribute");
        writeCommon(attribute);
        writeTypeRef(attribute.getType());
        writer.writeEndElement();
    }

    private void writeStructLink(Link link) {
        writer.writeStartElement("link");
        writeCommon(link);
        writeTypeRef(link.getType());
        writer.writeEndElement();
    }

    private void writeEnumType(EnumType type) {
        writer.writeStartElement("enum");
        writeCommon(type);
        type.values().forEach(this::writeEnumValue);
        writer.writeEndElement();
    }

    private void writeEnumValue(EnumValue value) {
        writer.writeStartElement("value");
        writeCommon(value);
        writer.writeEndElement();
    }

    private void writeService(Service service) {
        writer.writeStartElement("service");
        writeCommon(service);
        service.methods().forEach(this::writeServiceMethod);
        service.locators().forEach(this::writeServiceLocator);
        writer.writeEndElement();
    }

    private void writeServiceMethod(Method method) {
        writer.writeStartElement("method");
        writeCommon(method);
        method.parameters().forEach(this::writeParameter);
        writer.writeEndElement();
    }

    private void writeServiceLocator(Locator locator) {
        writer.writeStartElement("locator");
        writeCommon(locator);
        locator.parameters().forEach(this::writeParameter);
        writer.writeEndElement();
    }

    private void writeParameter(Parameter parameter) {
        writer.writeStartElement("parameter");
        writeCommon(parameter);
        writer.writeElement("in", Boolean.toString(parameter.isIn()));
        writer.writeElement("out", Boolean.toString(parameter.isIn()));
        writeTypeRef(parameter.getType());
        writer.writeEndElement();
    }

    private void writeCommon(Concept concept) {
        writeDoc(concept);
        writeName(concept);
    }

    private void writeName(Concept concept) {
        Name name = concept.getName();
        if (name != null) {
            writer.writeElement("name", name.toString());
        }
    }

    private void writeDoc(Concept concept) {
        String doc = concept.getDoc();
        if (doc != null) {
            writer.writeElement("doc", doc);
        }
    }

    private void writeTypeRef(Type type) {
        writer.writeElement("type", getTypeRef(type));
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
}

