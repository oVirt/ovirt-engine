package org.ovirt.engine.api.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.Assert;
import org.junit.Test;

public class ReflectionHelperTest {
    @Test
    public void getClasses() throws ClassNotFoundException, IOException {
        List<Class<?>> classes = ReflectionHelper.getClasses(ReflectionHelper.class.getPackage().getName());

        Assert.assertFalse("ReflectionHelper's package can not be empty since at least Reflectionhelper is in it",
                classes.isEmpty());
        Assert.assertTrue("ReflectionHelper's should be in it's own package", classes.contains(ReflectionHelper.class));
    }

    @Test
    public void getClassNamesInJarPackage() throws IOException {
        File temp = File.createTempFile("test-", ".jar");
        JarOutputStream jar = new JarOutputStream(new FileOutputStream(temp));
        ZipEntry helloworld =
                new ZipEntry(ReflectionHelperTest.class.getName().replaceAll("\\.", "/") + ".class");
        jar.putNextEntry(helloworld);
        jar.write("fake".getBytes("UTF-8"));
        jar.close();

        for(int i = 0; i < 2048; i++) {
            ReflectionHelper.getClassNamesInJarPackage(Thread.currentThread().getContextClassLoader(),
                    temp.getAbsolutePath(),
                    ReflectionHelperTest.class.getPackage().getName());
        }

        temp.delete();
    }
}
