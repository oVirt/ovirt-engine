package org.ovirt.engine.core.common.utils.ansible;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FileRemover {
    private static final Logger log = LoggerFactory.getLogger(FileRemover.class);

    public void removeFile(Path path) {
        if (path != null && !path.equals(Paths.get("/dev/null"))) {
            try {
                Files.delete(path);
            } catch (IOException ex) {
                log.debug("Failed to delete temporary file '{}': {}", path, ex.getMessage());
            }
        }
    }
}
