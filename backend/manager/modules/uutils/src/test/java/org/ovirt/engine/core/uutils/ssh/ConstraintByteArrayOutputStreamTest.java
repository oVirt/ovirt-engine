package org.ovirt.engine.core.uutils.ssh;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * SSHClient.ConstraintByteArrayOutputStream tests.
 */
public class ConstraintByteArrayOutputStreamTest {
    @Test
    public void testLimit() throws IOException {
        int limit = 1000;
        ByteArrayOutputStream os = new ConstraintByteArrayOutputStream(limit);
        byte[] buffer = new byte[100];
        for (int i = 0; i < limit * 5 / buffer.length; i++) {
            os.write(buffer);
        }
        assertTrue(os.size() >= limit);
        assertTrue(os.size() < limit * 3 / 2);
    }
}
