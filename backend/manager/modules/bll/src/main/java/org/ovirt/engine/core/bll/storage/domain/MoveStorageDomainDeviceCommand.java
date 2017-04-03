package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.common.action.MoveStorageDomainDeviceCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.MoveStorageDomainDeviceVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class MoveStorageDomainDeviceCommand<T extends MoveStorageDomainDeviceCommandParameters>
        extends StorageJobCommand<T> {

    public MoveStorageDomainDeviceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        MoveStorageDomainDeviceVDSCommandParameters p = new MoveStorageDomainDeviceVDSCommandParameters(getParameters().getStorageJobId(),
                getParameters().getStorageDomainId(),
                getParameters().getSrcDeviceId(),
                getParameters().getDstDevicesIds());
        p.setVdsId(getParameters().getVdsRunningOn());
        vdsCommandsHelper.runVdsCommandWithoutFailover(VDSCommandType.MoveStorageDomainDevice,
                p,
                getParameters().getStoragePoolId(),
                this);
        setSucceeded(true);
    }

    @Override
    public HostJobStatus handleJobError(EngineError error) {
        if (error == EngineError.NoSuchPhysicalVolume) {
            return HostJobStatus.done;
        }
        return HostJobStatus.failed;
    }
}
