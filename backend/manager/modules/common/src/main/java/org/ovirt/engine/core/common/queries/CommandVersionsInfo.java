package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Version;

/**
 * Holds version information on a command
 */
public class CommandVersionsInfo implements Serializable {

    private static final long serialVersionUID = -5493400076732533331L;

    public CommandVersionsInfo(String storagePoolVersion, String clusterVersion) {
        this(new Version(storagePoolVersion), new Version(clusterVersion));
    }

    public CommandVersionsInfo(Version storagePoolVersion, Version clusterVersion) {
        this.storagePoolVersion = storagePoolVersion;
        this.clusterVersion = clusterVersion;
    }

    public CommandVersionsInfo() {
    }

    private Version storagePoolVersion;
    private Version clusterVersion;

    public Version getStoragePoolVersion() {
        return storagePoolVersion;
    }

    public void setStoragePoolVersion(Version storagePoolVersion) {
        this.storagePoolVersion = storagePoolVersion;
    }

    public Version getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(Version clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

}
