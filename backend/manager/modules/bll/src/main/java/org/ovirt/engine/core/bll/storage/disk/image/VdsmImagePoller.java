package org.ovirt.engine.core.bll.storage.disk.image;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class VdsmImagePoller {
    private static final Logger log = LoggerFactory.getLogger(VdsmImagePoller.class);

    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    private VdsmImagePoller() {
    }

    protected HostJobStatus pollImage(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId,
                                      int executionGeneration, Guid cmdId, ActionType actionType) {
        Image imageInfo =
                ((DiskImage) vdsCommandsHelper.runVdsCommandWithoutFailover(
                        VDSCommandType.GetVolumeInfo,
                        new GetVolumeInfoVDSCommandParameters(storagePoolId,
                                storageDomainId,
                                imageGroupId,
                                imageId), storagePoolId, null).getReturnValue()).getImage();
        if (imageInfo.getLeaseStatus() != null && !imageInfo.getLeaseStatus().isFree()) {
            log.info("Command {} id: '{}': the volume lease is not FREE - the job is running",
                    actionType, cmdId);
            return HostJobStatus.running;
        }

        if (imageInfo.getGeneration() == executionGeneration + 1) {
            log.info("Command {} id: '{}': the volume lease is free and the generation was incremented - the " +
                            "job execution has completed successfully",
                    actionType, cmdId);
            return HostJobStatus.done;
        }

        if (imageInfo.getGeneration() == executionGeneration + StorageConstants.ENTITY_FENCING_GENERATION_DIFF) {
            log.info("Command {} id: '{}': the volume generation was incremented by the job fencing diff - the job " +
                            "was fenced and its status can be considered as failed",
                    actionType,
                    cmdId);
            return HostJobStatus.failed;
        }

        log.info("Command {} id: '{}': couldn't determine the status of the job by entity polling", actionType, cmdId);
        return null;
    }
}
