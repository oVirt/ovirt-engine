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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

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
 * This class takes a model and generates its JSON description.
*/
@ApplicationScoped
public class JsonDescriptionGenerator {
    private Model model;
    private JsonGenerator writer;

    public void generate(Model model, File file) {
        // Save the model:
        this.model = model;

        // Create the JSON writer:
        try (OutputStream out = new FileOutputStream(file)) {
            Map<String, Object> configuration = new HashMap<>();
            configuration.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonGeneratorFactory factory = Json.createGeneratorFactory(configuration);
            writer = factory.createGenerator(out, StandardCharsets.UTF_8);
            writeModel();
            writer.close();
        }
        catch (IOException exception) {
            throw new IllegalStateException("Can't generate JSON representation", exception);
        }
    }

    private void writeModel() {
        writer.writeStartObject();
        writer.writeStartArray("types");
        model.types().forEach(this::writeType);
        writer.writeEnd();
        writer.writeStartArray("services");
        model.services().forEach(this::writeService);
        writer.writeEnd();
        writer.writeEnd();
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
        writer.writeStartObject();
        writer.write("kind", "primitive");
        writeCommon(type);
        writer.writeEnd();
    }

    private void writeStructType(StructType type) {
        writer.writeStartObject();
        writer.write("kind", "struct");
        writeCommon(type);
        writer.writeStartArray("attributes");
        type.attributes().forEach(this::writeStructAttribute);
        writer.writeEnd();
        writer.writeStartArray("links");
        type.links().forEach(this::writeStructLink);
        writer.writeEnd();
        writer.writeEnd();
    }

    private void writeStructAttribute(Attribute attribute) {
        writer.writeStartObject();
        writeCommon(attribute);
        writeTypeRef(attribute.getType());
        writer.writeEnd();
    }

    private void writeStructLink(Link link) {
        writer.writeStartObject();
        writeCommon(link);
        writeTypeRef(link.getType());
        writer.writeEnd();
    }

    private void writeEnumType(EnumType type) {
        writer.writeStartObject();
        writer.write("kind", "enum");
        writeCommon(type);
        writer.writeStartArray("values");
        type.values().forEach(this::writeEnumValue);
        writer.writeEnd();
        writer.writeEnd();
    }

    private void writeEnumValue(EnumValue value) {
        writer.writeStartObject();
        writeCommon(value);
        writer.writeEnd();
    }

    private void writeService(Service service) {
        writer.writeStartObject();
        writeCommon(service);
        writer.writeStartArray("methods");
        service.methods().forEach(this::writeServiceMethod);
        writer.writeEnd();
        writer.writeStartArray("locators");
        service.locators().forEach(this::writeServiceLocator);
        writer.writeEnd();
        writer.writeEnd();
    }

    private void writeServiceMethod(Method method) {
        writer.writeStartObject();
        writeCommon(method);
        writer.writeStartArray("parameters");
        method.parameters().forEach(this::writeParameter);
        writer.writeEnd();
        writer.writeEnd();
    }

    private void writeServiceLocator(Locator locator) {
        writer.writeStartObject();
        writeCommon(locator);
        writer.writeStartArray("parameters");
        locator.parameters().forEach(this::writeParameter);
        writer.writeEnd();
        writer.writeEnd();
    }

    private void writeParameter(Parameter parameter) {
        writer.writeStartObject();
        writeCommon(parameter);
        writer.write("in", parameter.isIn());
        writer.write("out", parameter.isOut());
        writeTypeRef(parameter.getType());
        writer.writeEnd();
    }

    private void writeCommon(Concept concept) {
        writeDoc(concept);
        writeName(concept);
    }

    private void writeName(Concept concept) {
        Name name = concept.getName();
        if (name != null) {
            writer.write("name", name.toString());
        }
    }

    private void writeDoc(Concept concept) {
        String doc = concept.getDoc();
        if (doc != null) {
            writer.write("doc", doc);
        }
    }

    private void writeTypeRef(Type type) {
        writer.write("type", getTypeRef(type));
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

