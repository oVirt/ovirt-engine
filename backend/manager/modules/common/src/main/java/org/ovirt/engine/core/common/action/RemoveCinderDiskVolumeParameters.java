package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;

public class RemoveCinderDiskVolumeParameters extends RemoveImageParameters {
    private CinderDisk removedVolume;

    public RemoveCinderDiskVolumeParameters() {
    }

    public RemoveCinderDiskVolumeParameters(CinderDisk removedVolume) {
        setRemovedVolume(removedVolume);
    }

    public CinderDisk getRemovedVolume() {
        return removedVolume;
    }

    public void setRemovedVolume(CinderDisk removedVolume) {
        this.removedVolume = removedVolume;
    }
}
