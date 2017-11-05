package org.ovirt.engine.core.common.action;

public enum RemoveSnapshotSingleDiskStep {
    PREPARE_MERGE,
    EXTEND,
    MERGE,
    FINALIZE_MERGE,
    MERGE_STATUS,
    DESTROY_IMAGE,
    REDUCE_IMAGE,
    COMPLETE,
}
