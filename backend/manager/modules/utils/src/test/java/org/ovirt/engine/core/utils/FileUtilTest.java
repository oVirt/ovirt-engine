package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;

import org.junit.Test;

public class FileUtilTest {

    @Test
    public void testReadAllText() throws Exception {
        File iFile = File.createTempFile("Test", ".txt");
        try (FileWriter iFileWriter = new FileWriter(iFile)) {
            iFileWriter.write("This is a test");
        }

        String data = FileUtil.readAllText(iFile.getAbsolutePath());
        assertEquals("Data should be equal", "This is a test", data);
    }

    /**
     * Test to verify fix on File.ReadAllText()
     */
    @Test
    public void testReadAllTextTonSOfTimes() throws Exception {
        File iFile = File.createTempFile("Test", ".txt");
        try (FileWriter iFileWriter = new FileWriter(iFile)) {
            iFileWriter.write("This is a test");
        }

        for (int i = 0; i < 10000; i++) {
            String data = FileUtil.readAllText(iFile.getAbsolutePath());
            assertEquals("Data should be equal", "This is a test", data);
        }
    }
}
