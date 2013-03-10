package org.ovirt.engine.core.utils;

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

public class FileUtilTest extends TestCase {

    public void testReadAllText() throws Exception {
        File iFile = File.createTempFile("Test", ".txt");
        FileWriter iFileWriter = new FileWriter(iFile);
        iFileWriter.write("This is a test");
        iFileWriter.close();

        String data = FileUtil.readAllText(iFile.getAbsolutePath());
        assertEquals("Data should be equal", "This is a test", data);
    }

    /**
     * Test to verify fix on File.ReadAllText()
     */
    public void testReadAllTextTonSOfTimes() throws Exception {
        File iFile = File.createTempFile("Test", ".txt");
        FileWriter iFileWriter = new FileWriter(iFile);
        iFileWriter.write("This is a test");
        iFileWriter.close();

        for (int i = 0; i < 10000; i++) {
            String data = FileUtil.readAllText(iFile.getAbsolutePath());
            assertEquals("Data should be equal", "This is a test", data);
        }
    }
}
