package org.ovirt.engine.core.common.job;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;

/**
 * The {@code StepEnum} stores the name of steps which should be resolved as a descriptive message.
 */
public enum StepEnum {
    VALIDATING,
    EXECUTING,
    FINALIZING,
    INSTALLING_HOST,
    TEST_POWER_MANAGEMENT,
    ADD_VM_TO_POOL,
    MIGRATE_VM,
    CREATING_SNAPSHOTS,
    RUN_STATELESS_VM,
    TAKING_VM_FROM_POOL,
    REMOVING_VM,
    ADD_VM,

    // Gluster
    SETTING_GLUSTER_OPTION,
    REBALANCING_VOLUME,
    REMOVING_BRICKS,

    // Storage
    ALLOCATE_VOLUME,

    /**
     * Maps VDSM tasks type to {@code StepEnum} so it can be resolvable as readable description
     */
    COPY_IMAGE(AsyncTaskType.copyImage),
    MOVE_IMAGE(AsyncTaskType.moveImage),
    CREATE_VOLUME(AsyncTaskType.createVolume),
    DELETE_VOLUME(AsyncTaskType.deleteVolume),
    DELETE_IMAGE(AsyncTaskType.deleteImage),
    MERGE_SNAPSHOTS(AsyncTaskType.mergeSnapshots),
    CLONE_IMAGE_STRUCTURE(AsyncTaskType.cloneImageStructure),
    SYNC_IMAGE_DATA(AsyncTaskType.syncImageData),
    DOWNLOAD_IMAGE(AsyncTaskType.downloadImage),
    DOWNLOAD_IMAGE_FROM_STREAM(AsyncTaskType.downloadImageFromStream),
    UNKNOWN(AsyncTaskType.unknown);

    /**
     * Holds the map between the VDSM task to the Step name which represents it.
     */
    private static final Map<AsyncTaskType, StepEnum> asyncTaskToStepEnumMap = new HashMap<>();

    /**
     * Store the correlated VDSM task type with the step name or {@code null} if none.
     */
    private AsyncTaskType asyncTaskType;

    static {
        for (StepEnum s : values()) {
            if (s.asyncTaskType != null) {
                asyncTaskToStepEnumMap.put(s.asyncTaskType, s);
            }
        }
    }

    private StepEnum() {
    }

    private StepEnum(AsyncTaskType asyncTaskType) {
        this.asyncTaskType = asyncTaskType;
    }

    public static StepEnum getStepNameByTaskType(AsyncTaskType asyncTaskType) {
        return asyncTaskToStepEnumMap.get(asyncTaskType);
    }

}
