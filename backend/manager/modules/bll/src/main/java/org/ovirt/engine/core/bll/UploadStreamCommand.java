package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.UploadStreamVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class UploadStreamCommand<T extends UploadStreamParameters> extends ImageSpmCommand<T> {

    public UploadStreamCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public UploadStreamCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected VDSReturnValue executeVdsCommand() {
        UploadStreamVDSCommandParameters vdsCommandParameters =
                new UploadStreamVDSCommandParameters(
                        getVdsId(),
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getImageGroupID(),
                        getParameters().getImageId(),
                        getParameters().getStreamLength(),
                        getParameters().getInputStream());

        return runVdsCommand(VDSCommandType.UploadStream, vdsCommandParameters);
    }

    @Override
    protected boolean commandSpecificCanDoAction() {
        DiskImage targetDisk = getDiskImage();
        //Currently we'd like to support only preallocated disks to avoid possible extend on vdsm side.
        if (targetDisk.getVolumeType() != VolumeType.Preallocated) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_VOLUME_TYPE_UNSUPPORTED,
                    String.format("$volumeType %1$s", targetDisk.getVolumeType().toString()),
                    String.format("$supportedVolumeTypes %1$s", VolumeType.Preallocated));
        }

        return true;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.downloadImageFromStream;
    }
}
