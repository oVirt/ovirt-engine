package org.ovirt.engine.core.bll.storage.disk.image;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AllocateImageGroupVolumeCommandParameters;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.vdscommands.AllocateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.BaseDiskDao;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class AllocateImageGroupVolumeCommand<T extends AllocateImageGroupVolumeCommandParameters> extends
        StorageJobCommand<T> {

    @Inject
    private BaseDiskDao baseDiskDao;

    public AllocateImageGroupVolumeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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
            addCustomValue("DiskId", getParameters().getImageGroupID().toString());
            addCustomValue("DiskAlias", getDisk().getDiskAlias());
            return AuditLogType.DISK_PREALLOCATION_FAILED;
        }

        return super.getAuditLogTypeValue();
    }

    private BaseDisk getDisk() {
        return baseDiskDao.get(getParameters().getImageGroupID());
    }
}

