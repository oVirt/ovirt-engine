package org.ovirt.engine.core.compat;

import java.io.FileWriter;
import java.util.Date;

import org.ovirt.engine.core.compat.backendcompat.File;
import org.ovirt.engine.core.compat.backendcompat.StreamReaderCompat;

import junit.framework.TestCase;

public class FileTest extends TestCase {

    public void testExists() throws Exception {
        java.io.File iFile = java.io.File.createTempFile("Test", ".txt");
        assertTrue("Temp files are there", File.Exists(iFile.getAbsolutePath()));
        assertFalse("Garbage should not be there", File.Exists("/foof/dkjfhsk/fsjhfkjds"));
    }

    public void testDelete() throws Exception {
        java.io.File iFile = java.io.File.createTempFile("Test", ".txt");
        File.Delete(iFile.getAbsolutePath());
        assertFalse("Deleted File should be gone", File.Exists(iFile.getAbsolutePath()));
    }

    public void testGetLastWriteTime() throws Exception {
        Date before = new Date();
        Thread.sleep(1000);
        java.io.File iFile = java.io.File.createTempFile("Test", ".txt");
        Thread.sleep(1000);
        Date after = new Date();
        Date creation = File.GetLastWriteTime(iFile.getAbsoluteFile());
        assertTrue("Creation should be after before ", creation.compareTo(before) > 0);
        assertTrue("Creation should be before after ", creation.compareTo(after) < 0);
    }

    public void testReadAllText() throws Exception {
        java.io.File iFile = java.io.File.createTempFile("Test", ".txt");
        FileWriter iFileWriter = new FileWriter(iFile);
        iFileWriter.write("This is a test");
        iFileWriter.close();

        String data = File.ReadAllText(iFile.getAbsolutePath());
        assertEquals("Data should be equal", "This is a test", data);
    }

    public void testOpenText() throws Exception {
        java.io.File iFile = java.io.File.createTempFile("Test", ".txt");
        FileWriter iFileWriter = new FileWriter(iFile);
        iFileWriter.write("This is a test");
        iFileWriter.close();

        StreamReaderCompat src = File.OpenText(iFile.getAbsolutePath());
        assertEquals("Data should be equal", "This is a test", src.ReadToEnd());
    }
}
