/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.rsdl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.ObjectFactory;
import org.ovirt.engine.api.model.Rsdl;
import org.ovirt.engine.api.utils.ApiRootLinksCreator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

public class RsdlManager {

    private static final String RSDL_DESCRIPTION = "The oVirt RESTful API description language.";
    private static final String QUERY_PARAMETER = "?";
    private static final String SCHEMA_DESCRIPTION = "oVirt API entities schema.";
    private static final String RSDL_REL = "rsdl";
    private static final String RSDL_CONSTRAINT_PARAMETER = "rsdl";
    private static final String SCHEMA_REL = "schema";
    private static final String SCHEMA_CONSTRAINT_PARAMETER = "schema";
    private static final String SCHEMA_NAME = "ovirt-engine-api-schema.xsd";
    private static final String GENERAL_METADATA_REL = "*";
    private static final String GENERAL_METADATA_NAME = "The oVirt RESTful API generic descriptor.";
    private static final String GENERAL_METADATA_DESCRIPTION = "These options are valid for entire application.";
    private static final String ILLEGAL_ACTION_LINK_SUFFIX = "/";

    private static final String METADATA_FILE_NAME = "/rsdl_metadata.yaml";

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        System.out.println("Generating RSDL files...");
        String baseUri = args[0];
        String outputFileName = args[1];
        String outputFileNameGluster = args[2];

        MetaData metadata = loadMetaData();
        validateActionLinksFormat(metadata);
        generateRsdlFile(metadata, outputFileName, ApiRootLinksCreator.getAllRels(baseUri));
        generateRsdlFile(metadata, outputFileNameGluster, ApiRootLinksCreator.getGlusterRels(baseUri));

        System.out.println("The following files have been generated: \n" + outputFileName + "\n"
                + outputFileNameGluster);
    }


    private static void validateActionLinksFormat(MetaData metadata) {
        List<String> illegalActionLinks = new ArrayList<>();
        for (Action action : metadata.getActions()) {
            String actionLink = action.getName().split("[|]")[0];
            if (actionLink.endsWith(ILLEGAL_ACTION_LINK_SUFFIX)) {
                illegalActionLinks.add(action.getName());
            }
        }

        if (!illegalActionLinks.isEmpty()) {
            throw new RuntimeException("Invalid link suffix:\n" + StringUtils.join(illegalActionLinks, '\n'));
        }
    }

    private static void generateRsdlFile(MetaData metadata, String outputFileName, List<String> rels)
            throws IOException, ClassNotFoundException {
        Rsdl rsdl = buildRsdl(metadata, rels);
        serializeRsdl(rsdl, outputFileName);
    }

    private static void serializeRsdl(Rsdl rsdl, String rsdlLocation) {
        ObjectFactory factory = new ObjectFactory();
        JAXBElement<Rsdl> element = factory.createRsdl(rsdl);
        JAXB.marshal(element, new File(rsdlLocation));
    }

    private static Rsdl buildRsdl(MetaData metadata, List<String> rels) throws IOException,
            ClassNotFoundException {
        RsdlBuilder builder = new RsdlBuilder(rels, metadata)
        .description(RSDL_DESCRIPTION)
        .rel(RSDL_REL)
                .href(QUERY_PARAMETER + RSDL_CONSTRAINT_PARAMETER)
        .schema(new SchemaBuilder()
            .rel(SCHEMA_REL)
            .href(QUERY_PARAMETER + SCHEMA_CONSTRAINT_PARAMETER)
            .name(SCHEMA_NAME)
            .description(SCHEMA_DESCRIPTION)
            .build())
        .generalMetadata(new GeneralMetadataBuilder()
            .rel(GENERAL_METADATA_REL)
            .href("*")
            .name(GENERAL_METADATA_NAME)
            .description(GENERAL_METADATA_DESCRIPTION)
            .build());
        Rsdl rsdl = builder.build();
        return rsdl;
    }

    private static MetaData loadMetaData() throws IOException {
        try (InputStream in = RsdlManager.class.getResourceAsStream(METADATA_FILE_NAME)) {
            return loadMetaData(in);
        }
    }

    private static MetaData loadMetaData(InputStream in) throws IOException {
        Constructor constructor = new CustomClassLoaderConstructor(Thread.currentThread().getContextClassLoader());
        MetaData metaData = (MetaData) new Yaml(constructor).load(in);
        if (metaData == null) {
            throw new IOException("Can't load metadata from input stream");
        }

        // Make sure that the loaded metadata contains default values:
        assignDefaults(metaData);

        // Remove leading slashes from all the action names:
        for (Action action : metaData.getActions()) {
            String name = action.getName();
            name = name.replaceAll("^/?", "");
            action.setName(name);
        }

        return metaData;
    }

    /**
     * This methods updates the loaded metadata so that it contains the default values instead of null references. For
     * example, the metadata file may not contain a list of signatures for a particular action, but we want to make sure
     * that it contains an empty list instead of a null reference.
     *
     * @param metaData the metadata whose default values will be assigned
     */
    private static void assignDefaults(MetaData metaData) {
        for (Action action : metaData.getActions()) {
            assignDefaults(action);
        }
    }

    /**
     * This methods updates the given action so that it contains the default values.
     *
     * @param action the action whose default values will be assigned
     */
    private static void assignDefaults(Action action) {
        // Create the request if needed:
        Request request = action.getRequest();
        if (request == null) {
            request = new Request();
        }
        action.setRequest(request);

        // Create the map of headers if needed:
        Map<String, ParamData> headers = request.getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
            request.setHeaders(headers);
        }

        // Create the map of URL parameters if needed:
        Map<String, ParamData> parameters = request.getUrlparams();
        if (parameters == null) {
            parameters = new HashMap<>();
            request.setUrlparams(parameters);
        }

        // Create the request body if needed:
        Body body = request.getBody();
        if (body == null) {
            body = new Body();
            request.setBody(body);
        }

        // Create the list of signatures if needed:
        List<Signature> signatures = body.getSignatures();
        if (signatures == null) {
            signatures = new ArrayList<>();
        }
        body.setSignatures(signatures);
    }
}
