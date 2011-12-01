package org.ovirt.engine.core.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import junit.framework.TestCase;

public class FileUtilTest extends TestCase {

    public void testFileExists() throws Exception {
        File iFile = File.createTempFile("Test", ".txt");
        assertTrue("Temp files are there", FileUtil.fileExists(iFile.getAbsolutePath()));
        assertFalse("Garbage should not be there", FileUtil.fileExists("/foof/dkjfhsk/fsjhfkjds"));
    }

    public void testDeleteFile() throws Exception {
        File iFile = File.createTempFile("Test", ".txt");
        FileUtil.deleteFile(iFile.getAbsolutePath());
        assertFalse("Deleted File should be gone", FileUtil.fileExists(iFile.getAbsolutePath()));
    }

    public void testGetLastWriteTime() throws Exception {
        Date before = new Date();
        Thread.sleep(1000);
        File iFile = File.createTempFile("Test", ".txt");
        Thread.sleep(1000);
        Date after = new Date();
        Date creation = FileUtil.getLastWriteTime(iFile.getAbsoluteFile());
        assertTrue("Creation should be after before ", creation.compareTo(before) > 0);
        assertTrue("Creation should be before after ", creation.compareTo(after) < 0);
    }

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
