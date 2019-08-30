package org.ovirt.engine.core.common.utils.ansible;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnsibleCommandInventoryFileFactory {

    @Inject
    private FileRemover fileRemover;

    private static final Logger log = LoggerFactory.getLogger(AnsibleCommandInventoryFileFactory.class);

    /**
     * Create a temporary inventory file if user didn't specify it.
     */
    public AutoRemovableTempFile create(InventoryFileConfig inventoryFileConfig) throws IOException {
        Path inventoryFile = inventoryFileConfig.inventoryFile();
        if (inventoryFile == null) {

            // If hostnames are empty we just don't pass any inventory file:
            if (CollectionUtils.isNotEmpty(inventoryFileConfig.hostnames())) {
                log.debug("Inventory hosts: {}", inventoryFileConfig.hostnames());
                inventoryFile = Files.createTempFile("ansible-inventory", "");
                Files.write(inventoryFile,
                        StringUtils.join(inventoryFileConfig.hostnames(), System.lineSeparator()).getBytes());
            }
        }

        if (inventoryFile != null) {
            return new AutoRemovableTempFile(inventoryFile, fileRemover);
        }

        return null;
    }
}
