package org.ovirt.engine.api.rsdl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RsdlIOManager {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    public static final String SCHEMA_RESOURCE_NAME = "api.xsd";
    public static final String RSDL_RESOURCE_NAME = "rsdl.xml";
    public static final String GLUSTER_RSDL_RESOURCE_NAME = "rsdl_gluster.xml";

    public static void main(String[] args) throws IOException {
        System.out.println("Copying rsdl.xml, rsdl_gluster.xml, api.xsd to: " + args[0]);
        copyRsdl(args[0]);
    }

    public static void copyRsdl(String outputDirectory) throws IOException {
        File outputDir = createOutputDirectory(outputDirectory);
        copy(RSDL_RESOURCE_NAME, outputDir);
        copy(GLUSTER_RSDL_RESOURCE_NAME, outputDir);
        copy(SCHEMA_RESOURCE_NAME, outputDir);
    }

    private static File createOutputDirectory(String outputDirectory) throws IOException {
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            boolean success = outputDir.mkdirs();
            if (!success) {
                throw new IOException("Falied to create directory: " + outputDirectory
                        + ". rsdl.xml will not be copied there.");
            }
        }
        return outputDir;
    }

    private static void copy(String resourceName, File outputDirectory) throws IOException {
        copy(loadAsStream("/" + resourceName), new FileOutputStream(new File(outputDirectory, resourceName)));
    }

    public static InputStream loadAsStream(String resourceName) {
        return RsdlIOManager.class.getResourceAsStream(resourceName);
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }
}
