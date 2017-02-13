package org.ovirt.engine.core.common.asynctasks;

public enum AsyncTaskType {
    unknown,
    copyImage,
    moveImage,
    createVolume,
    deleteVolume,
    deleteImage,
    mergeSnapshots,
    cloneImageStructure,
    syncImageData,
    extendImageSize,
    downloadImage,
    uploadImageToStream,
    downloadImageFromStream,
    prepareMerge,
    finalizeMerge,
    notSupported,
    addVmLease,
    removeVmLease;

    public int getValue() {
        return this.ordinal();
    }

    public static AsyncTaskType forValue(int value) {
        return values()[value];
    }
}
