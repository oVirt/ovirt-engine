package org.ovirt.engine.api.rsdl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.RSDL;
import org.ovirt.engine.api.utils.ApiRootLinksCreator;
import org.ovirt.engine.core.common.mode.ApplicationMode;
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
        generateRsdlFile(metadata, baseUri, outputFileName, ApiRootLinksCreator.getAllRels(baseUri));
        generateRsdlFile(metadata, baseUri, outputFileNameGluster, ApiRootLinksCreator.getGlusterRels(baseUri));

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

    private static void generateRsdlFile(MetaData metadata, String baseUri, String outputFileName, List<String> rels)
            throws IOException, ClassNotFoundException {
        RSDL rsdl = buildRsdl(metadata, rels, baseUri);
        serializeRsdl(rsdl, outputFileName);
    }

    public static RSDL loadRsdl(ApplicationMode applicationMode) throws IOException {
        String fileName =
                applicationMode == ApplicationMode.GlusterOnly ? ("/" + RsdlIOManager.GLUSTER_RSDL_RESOURCE_NAME)
                        : ("/" + RsdlIOManager.RSDL_RESOURCE_NAME);
        InputStream rsdlAsStrem = null;
        try {
            rsdlAsStrem = RsdlIOManager.loadAsStream(fileName);
            return JAXB.unmarshal(rsdlAsStrem, RSDL.class);
        } finally {
            if (rsdlAsStrem != null) {
                rsdlAsStrem.close();
            }
        }

    }

    private static void serializeRsdl(RSDL rsdl, String rsdlLocation) {
        JAXB.marshal(rsdl, new File(rsdlLocation));
    }

    private static RSDL buildRsdl(MetaData metadata, List<String> rels, String baseUri) throws IOException,
            ClassNotFoundException {
        RsdlBuilder builder = new RsdlBuilder(baseUri, rels, metadata)
        .description(RSDL_DESCRIPTION)
        .rel(RSDL_REL)
                .href(baseUri + QUERY_PARAMETER + RSDL_CONSTRAINT_PARAMETER)
        .schema(new SchemaBuilder()
                .rel(SCHEMA_REL)
                        .href(baseUri +
                        QUERY_PARAMETER + SCHEMA_CONSTRAINT_PARAMETER)
                .name(SCHEMA_NAME)
                .description(SCHEMA_DESCRIPTION)
                .build())
        .generalMetadata(new GeneralMetadataBuilder()
                .rel(GENERAL_METADATA_REL)
                        .href(baseUri.replace("api", "*"))
                .name(GENERAL_METADATA_NAME)
                .description(GENERAL_METADATA_DESCRIPTION)
                        .build());
        RSDL rsdl = builder.build();
        return rsdl;
    }

    private static MetaData loadMetaData() throws IOException {
        InputStream stream = RsdlManager.class.getResourceAsStream(METADATA_FILE_NAME);
        Constructor constructor = new CustomClassLoaderConstructor(Thread.currentThread().getContextClassLoader());
        Object result = new Yaml(constructor).load(stream);
        stream.close();
        MetaData metadata = (MetaData) result;
        return metadata;
    }
}
