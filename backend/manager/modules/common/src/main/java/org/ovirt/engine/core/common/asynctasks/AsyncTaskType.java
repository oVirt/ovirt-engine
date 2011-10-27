package org.ovirt.engine.core.common.asynctasks;

//    @XmlType(name="AsyncTaskType")
public enum AsyncTaskType {
    unknown,
    copyImage,
    moveImage,
    createVolume,
    deleteVolume,
    deleteImage,
    mergeSnapshots,
    moveMultipleImages;

    public int getValue() {
        return this.ordinal();
    }

    public static AsyncTaskType forValue(int value) {
        return values()[value];
    }
}
