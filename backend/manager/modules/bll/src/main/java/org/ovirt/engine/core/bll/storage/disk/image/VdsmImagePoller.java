package org.ovirt.engine.core.bll.storage.disk.image;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class VdsmImagePoller {
    private static final Logger log = LoggerFactory.getLogger(VdsmImagePoller.class);

    private VdsmImagePoller() {
    }

    protected HostJobStatus pollImage(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId,
                                      int executionGeneration, Guid cmdId, VdcActionType actionType) {
        Image imageInfo =
                ((DiskImage) VdsCommandsHelper.runVdsCommandWithoutFailover(
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

        if (imageInfo.getStatus() == ImageStatus.ILLEGAL) {
            log.info("Command {} id: '{}': the volume is in ILLEGAL status - the job has failed",
                    actionType, cmdId);
            return HostJobStatus.failed;
        }

        if (imageInfo.getStatus() == ImageStatus.OK && imageInfo.getGeneration() == executionGeneration + 1) {
            log.info("Command {} id: '{}': the volume is in OK status and the generation was incremented - the " +
                            "job execution has completed successfully",
                    actionType, cmdId);
            return HostJobStatus.done;
        }

        log.info("Command {} id: '{}': couldn't determine the status of the job by entity polling", actionType, cmdId);
        return null;
    }
}
