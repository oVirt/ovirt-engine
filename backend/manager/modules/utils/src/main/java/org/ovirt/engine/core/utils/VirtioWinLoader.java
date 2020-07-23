package org.ovirt.engine.core.utils;

public interface VirtioWinLoader {

    /**
     * This reading and loading from JSON file function.
     */
    void load();

    /**
     * This function will retrieve the available QEMU agent version
     * in the JSON file.
     * @param osId the ID of the Operation System.
     * @return String of the QEMU guest agent version.
     */
    String getAgentVersionByOsName(int osId);

    /**
     * Used in case we want to get the ISO name of VirtIO-Win.
     * @return The string of the ISO name containing the VirtIO-Win drivers.
     */
    String getVirtioIsoName();
}
