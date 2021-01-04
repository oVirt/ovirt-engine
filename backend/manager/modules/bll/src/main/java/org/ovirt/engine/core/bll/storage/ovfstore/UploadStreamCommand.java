package org.ovirt.engine.core.bll.storage.ovfstore;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.UploadStreamParameters;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImageSpmCommand;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.UploadStreamVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class UploadStreamCommand<T extends UploadStreamParameters> extends ImageSpmCommand<T> {

    public UploadStreamCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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
    protected boolean commandSpecificValidate() {
        DiskImage targetDisk = getDiskImage();
        // Currently we'd like to support preallocated disks only on block
        // storage domain in order to avoid possible extend on vdsm side.
        if (targetDisk.getStorageTypes().get(0).isBlockDomain() && targetDisk.getVolumeType() != VolumeType.Preallocated) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_VOLUME_TYPE_UNSUPPORTED,
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
