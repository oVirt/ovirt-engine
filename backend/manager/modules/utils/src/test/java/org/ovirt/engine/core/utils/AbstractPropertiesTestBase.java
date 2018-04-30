package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AbstractPropertiesTestBase<E extends Enum<E>> {
    private Class<E> enumClass;
    private String relativePath;
    private File file;

    protected AbstractPropertiesTestBase(Class<E> enumClass, String relativePath) {
        this.enumClass = enumClass;
        this.relativePath = relativePath;
    }

    @BeforeEach
    public void loadFileFromPath() {
        String baseDir =  System.getProperty("basedir");
        assumeTrue(baseDir != null);

        file = new File(baseDir, relativePath);
    }

    @Test
    public void testRedundantMessages() throws IOException {
        EnumTranslationProperties props = new EnumTranslationProperties(enumClass);
        try (InputStream is = new FileInputStream(file)) {
            props.load(is);
        } catch (MissingEnumTranslationException exception) {
            fail("Check for redundant keys in " + file.getAbsolutePath() + " failed: " + exception.getMessage());
        }
    }
}
