package org.ovirt.engine.core.common.utils.ansible;

import java.io.Closeable;
import java.nio.file.Path;

public class AutoRemovableTempFile implements Closeable {
    private final Path filePath;
    private final FileRemover fileRemover;

    /**
     * @param filePath
     *            path to file which will be removed during close(), null is allowed
     * @param fileRemover
     *            lib for removing files under provided path
     */
    public AutoRemovableTempFile(Path filePath, FileRemover fileRemover) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path must not be null");
        }

        this.fileRemover = fileRemover;
        this.filePath = filePath;
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public void close() {
        fileRemover.removeFile(filePath);
    }
}
