package org.ovirt.engine.core.common.action;

public enum RemoveSnapshotSingleDiskLiveStep {
    EXTEND,
    MERGE,
    MERGE_STATUS,
    DESTROY_IMAGE,
    DESTROY_IMAGE_CHECK,
    COMPLETE,
}
