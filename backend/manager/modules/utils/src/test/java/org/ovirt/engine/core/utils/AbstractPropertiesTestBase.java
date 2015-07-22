package org.ovirt.engine.core.utils;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class AbstractPropertiesTestBase {
    private String relativePath;
    private File file;

    public AbstractPropertiesTestBase(String relativePath) {
        this.relativePath = relativePath;
    }

    @Before
    public void loadFileFromPath() {
        String baseDir =  System.getProperty("basedir");
        assumeNotNull(baseDir);

        file = new File(baseDir, relativePath);
    }

    @Test
    public void testDuplicateKeys() throws IOException {
        NoDuplicateProperties props = new NoDuplicateProperties();
        try (InputStream is = new FileInputStream(file)) {
            props.load(is);
        }
        catch (DuplicatePropertyException exception) {
            fail("Check for duplicate keys in " + file.getAbsolutePath() + " failed: " + exception.getMessage());
        }
    }

    @Test
    public void testRedundantMessages() throws IOException {
        EnumTranslationProperties props = new EnumTranslationProperties(EngineMessage.class, EngineError.class);
        try (InputStream is = new FileInputStream(file)) {
            props.load(is);
        } catch (MissingEnumTranslationException exception) {
            fail("Check for redundant keys in " + file.getAbsolutePath() + " failed: " + exception.getMessage());
        }
    }
}
