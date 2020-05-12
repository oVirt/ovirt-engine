package org.ovirt.engine.core.utils;

import java.io.IOException;
import java.nio.file.Path;

public interface VirtioWinLoader {

    /**
     * This reading and loading from JSON file function.
     * @param directoryPath the path to the JSON file.
     * @throws IOException in case there is an error reading the JSON.
     */
    void load(Path directoryPath) throws IOException;

    /**
     * This function will retrieve the available QEMU agent version
     * in the JSON file.
     * @param osId the ID of the Operation System.
     * @return String of the QEMU guest agent version.
     */
    String getAgentVersionByOsName(int osId);
}
