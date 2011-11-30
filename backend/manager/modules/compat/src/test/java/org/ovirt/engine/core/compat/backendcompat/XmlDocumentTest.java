package org.ovirt.engine.core.compat.backendcompat;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class XmlDocumentTest {
    @Test
    public void Load() throws IOException {
        final java.io.File temp = java.io.File.createTempFile("test-", ".xml");
        try {
            FileWriter writer = new FileWriter(temp);
            writer.write("<?xml version=\"1.0\"?>\n <foo> <bar/> <!-- comment --> </foo>");
            writer.close();

            final XmlDocument document = new XmlDocument();
            for (int i = 0; i < 2048; i++) {
                document.Load(temp.getAbsolutePath());
            }

        } finally {
            temp.delete();
        }

    }
}
