package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AllocateImageGroupVolumeCommandParameters;
import org.ovirt.engine.core.common.vdscommands.AllocateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class AllocateImageGroupVolumeCommand<T extends AllocateImageGroupVolumeCommandParameters> extends
        StorageJobCommand<T> {

    public AllocateImageGroupVolumeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setImageId(getParameters().getImageId());
        setImageGroupId(getParameters().getImageGroupID());
    }

    public AllocateImageGroupVolumeCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected void executeCommand() {
        VdsCommandsHelper.runVdsCommandWithFailover(VDSCommandType.AllocateVolume,
                new AllocateVolumeVDSCommandParameters(getParameters().getStorageDomainId(),
                        getParameters().getJobId(),
                        getParameters().getImageGroupID(),
                        getParameters().getImageId(),
                        getParameters().getSize()), getParameters().getStoragePoolId(), this);
        setSucceeded(true);
    }

    public AuditLogType getAuditLogTypeValue() {
        if (!getSucceeded()) {
            addCustomValue("DiskId", getDiskImage().getId().toString());
            addCustomValue("DiskAlias", getDiskImage().getDiskAlias());
            return AuditLogType.DISK_PREALLOCATION_FAILED;
        }

        return super.getAuditLogTypeValue();
    }
}

