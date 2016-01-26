package org.ovirt.engine.core.uutils.config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class ShellLikeConfdTest {

    private static ShellLikeConfd config;

    @BeforeClass
    public static void beforeClass() throws Exception {
        config = new ShellLikeConfd();

        config.loadConfig(
                URLDecoder.decode(ClassLoader.getSystemResource("config.conf").getPath(), "UTF-8"),
                "/dev/null"
        );
    }

    @Test
    public void testValid() throws Exception {

        List<String> res = new LinkedList<>();
        for (Map.Entry<String, String> e : config.getProperties().entrySet()) {
            res.add(String.format("%s=%s", e.getKey(), e.getValue()));
        }
        Collections.sort(res);

        String reference;
        InputStream in = null;
        try {
            in = new FileInputStream(URLDecoder.decode(ClassLoader.getSystemResource("config.conf.ref").getPath(), "UTF-8"));
            byte[] buffer = new byte[2048];
            int size = in.read(buffer);
            reference = new String(buffer, 0, size, StandardCharsets.UTF_8);
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {}
        }
        for (Object o:res.toArray()){
            System.out.println(o);
        }
        for (Object o:reference.split("\n")){
            System.out.println(o);
        }

        assertArrayEquals(reference.split("\n"), res.toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSuffixedProperty() throws Exception {

        assertEquals(config.getProperty("key00", "non_existent", false), "value0");
        assertEquals(config.getProperty("key01", "suffixed", false), "suffixed val");
        assertEquals(config.getProperty("non_existent", "non_existent", false), "throws exception");
    }
}
