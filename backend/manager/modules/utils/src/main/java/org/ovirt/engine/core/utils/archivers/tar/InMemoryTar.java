package org.ovirt.engine.core.utils.archivers.tar;

import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class InMemoryTar implements AutoCloseable{
    private TarArchiveOutputStream tarArchiveOutputStream;

    public InMemoryTar(OutputStream outputStream) {
        tarArchiveOutputStream = new TarArchiveOutputStream(outputStream);
    }

    public void addTarEntry(byte[] data, String name) throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(data.length);
        tarArchiveOutputStream.putArchiveEntry(entry);
        tarArchiveOutputStream.write(data);
        tarArchiveOutputStream.closeArchiveEntry();
    }

    @Override
    public void close() throws Exception {
        tarArchiveOutputStream.close();
    }
}
