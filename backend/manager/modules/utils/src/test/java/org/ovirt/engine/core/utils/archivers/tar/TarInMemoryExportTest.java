package org.ovirt.engine.core.utils.archivers.tar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class TarInMemoryExportTest {

    @Test
    public void untarEmptyFileReturnsNoContent() throws IOException {
        byte[] emptyFile = new byte[0];
        try (TarInMemoryExport tar = new TarInMemoryExport(new ByteArrayInputStream(emptyFile))) {
            Map<String, ByteBuffer> emptyTarEntries = tar.unTar();
            assertTrue(emptyTarEntries.isEmpty());
        }
    }

    @Test
    public void untarEmptyTarArchiveReturnsEntries() throws IOException {
        byte[] emptyFile = load("tar-with-empty-file.tar");
        try (TarInMemoryExport tar = new TarInMemoryExport(new ByteArrayInputStream(emptyFile))) {
            Map<String, ByteBuffer> entries = tar.unTar();
            assertThat(entries.size(), is(1));
            assertThat(entries, hasKey("empty.file"));
        }
    }

    @Test
    public void untarSuccessfully() throws IOException {
        byte[] nonEmptyTar = load("non-empty.tar");
        try (TarInMemoryExport tar = new TarInMemoryExport(new ByteArrayInputStream(nonEmptyTar))) {
            Map<String, ByteBuffer> emptyTarEntries = tar.unTar();
            assertThat(emptyTarEntries.entrySet(), not(empty()));
            assertThat(emptyTarEntries, hasKey("test.conf"));
            assertThat(emptyTarEntries.get("test.conf"), notNullValue());
        }
    }

    @Test
    public void untarAndSkipBadEntries() throws IOException {
        byte[] mixedContentArchive = load("mixed-content.tar");
        try (TarInMemoryExport tar = new TarInMemoryExport(new ByteArrayInputStream(mixedContentArchive))) {
            Map<String, ByteBuffer> entries = tar.unTar();
            assertThat(entries.entrySet(), not(empty()));
            assertThat(entries, hasKey("file.conf"));
            assertThat(entries.get("file.conf"), notNullValue());
            assertThat(entries, hasKey("empty.file"));
        }
    }

    private byte[] load(String file) {
        try {
            Path path = Paths.get(ClassLoader.getSystemResource(file).toURI());
            return Files.readAllBytes(path);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

}
