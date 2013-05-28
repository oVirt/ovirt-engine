package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class LocalConfigTest {

    @Test
    public void testValid() throws Exception {
        LocalConfig config = new LocalConfig();
        config.loadConfig(
            URLDecoder.decode(ClassLoader.getSystemResource("localconfig.conf").getPath(), "UTF-8"),
            "/dev/null"
        );
        List<String> res = new LinkedList<String>();
        for (Map.Entry<String, String> e : config.getProperties().entrySet()) {
            res.add(String.format("%s=%s", e.getKey(), e.getValue()));
        }
        Collections.sort(res);

        String reference;
        InputStream in = null;
        try {
            in = new FileInputStream(URLDecoder.decode(ClassLoader.getSystemResource("localconfig.conf.ref").getPath(), "UTF-8"));
            byte buffer[] = new byte[2048];
            int size = in.read(buffer);
            reference = new String(buffer, 0, size, Charset.forName("UTF-8"));
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {}
        }

        assertEquals(reference.split("\n"), res.toArray());
    }
}
