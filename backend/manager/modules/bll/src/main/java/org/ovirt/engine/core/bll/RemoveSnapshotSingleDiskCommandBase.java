package org.ovirt.engine.core.bll;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public abstract class RemoveSnapshotSingleDiskCommandBase<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {
    protected RemoveSnapshotSingleDiskCommandBase(T parameters) {
        this(parameters, null);
    }


    protected RemoveSnapshotSingleDiskCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Disk.name().toLowerCase(), getDiskImage().getDiskAlias());
            jobProperties.put("sourcesnapshot",
                    getSnapshotDescriptionById(getDiskImage().getVmSnapshotId()));
            jobProperties.put("destinationsnapshot",
                    getSnapshotDescriptionById(getDestinationDiskImage().getVmSnapshotId()));
        }
        return jobProperties;
    }

    protected void updateDiskImageDynamic(final DiskImage targetImage) {
        VDSReturnValue ret = runVdsCommand(
                VDSCommandType.GetImageInfo,
                new GetImageInfoVDSCommandParameters(targetImage.getStoragePoolId(),
                        targetImage.getStorageIds().get(0),
                        targetImage.getId(),
                        targetImage.getImageId()));

        // Update image's actual size in DB
        DiskImage imageFromIRS = (DiskImage) ret.getReturnValue();
        if (imageFromIRS != null) {
            completeImageData(imageFromIRS);
        } else {
            log.warnFormat("Could not update DiskImage's size with ID {0}",
                    targetImage.getImageId());
        }
    }

    @Override
    protected void endWithFailure() {
        // TODO: FILL! We should determine what to do in case of
        // failure (is everything rolled-backed? rolled-forward?
        // some and some?).
        setSucceeded(true);
    }

    private String getSnapshotDescriptionById(Guid snapshotId) {
        Snapshot snapshot = getSnapshotDao().get(snapshotId);
        return snapshot != null ? snapshot.getDescription() : StringUtils.EMPTY;
    }
}
