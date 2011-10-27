package org.ovirt.engine.core.compat;

import java.io.File;

import org.ovirt.engine.core.compat.backendcompat.Path;

import junit.framework.TestCase;

public class PathTest extends TestCase {
    public void testPathRooted() {
        if (System.getProperty("os.name").startsWith("Win")) {
            assertTrue("c:\\foo\\bar should be rooted", Path.IsPathRooted("c:\\foo\\bar"));
            assertFalse("foo\\bar should not be rooted", Path.IsPathRooted("foo\\bar"));
        } else {
            assertTrue("/foo/bar should be rooted", Path.IsPathRooted("/foo/bar"));
            assertFalse("foo/bar should not be rooted", Path.IsPathRooted("foo/bar"));
        }
    }

    public void testGetDirectoryName() {
        String sep = File.separator;
        String dir = sep + "Jar" + sep + "Jar";
        assertEquals("1", dir, Path.GetDirectoryName("/Jar/Jar/Binks.java"));
        assertEquals("2", dir, Path.GetDirectoryName("/Jar/Jar/Binks"));
    }

    public void testCombine() {
        if (System.getProperty("os.name").startsWith("Win")) {
            assertEquals("1", "c:\\Jar\\Jar\\Binks", Path.Combine("c:\\Jar\\Jar", "Binks"));
        } else {
            assertEquals("1", "/Jar/Jar/Binks", Path.Combine("/Jar/Jar", "Binks"));
        }
    }
}
