package org.ovirt.engine.api.rsdl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXB;

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

    private static final String METADATA_FILE_NAME = "/rsdl_metadata.yaml";
    private static final String RSDL_FILE_NAME = "/rsdl.xml";
    private static final String GLUSTER_RSDL_FILE_NAME = "/rsdl_gluster.xml";

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        System.out.println("Generating RSDL files...");
        String baseUri = args[0];
        String outputFileName = args[1];
        String outputFileNameGluster = args[2];

        MetaData metadata = loadMetaData();
        generateRsdlFile(metadata, baseUri, outputFileName, ApiRootLinksCreator.getAllRels(baseUri));
        generateRsdlFile(metadata, baseUri, outputFileNameGluster, ApiRootLinksCreator.getGlusterRels(baseUri));

        System.out.println("The following files have been generated: \n" + outputFileName + "\n"
                + outputFileNameGluster);
    }

    private static void generateRsdlFile(MetaData metadata, String baseUri, String outputFileName, List<String> rels)
            throws IOException, ClassNotFoundException {
        RSDL rsdl = buildRsdl(metadata, rels, baseUri);
        serializeRsdl(rsdl, outputFileName);
    }

    public RSDL loadRsdl(ApplicationMode applicationMode) throws IOException {
        InputStream stream =
                applicationMode == ApplicationMode.GlusterOnly ?
                        this.getClass().getResourceAsStream(GLUSTER_RSDL_FILE_NAME) :
                        this.getClass().getResourceAsStream(RSDL_FILE_NAME);
        return JAXB.unmarshal(stream, RSDL.class);
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
