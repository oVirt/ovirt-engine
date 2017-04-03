package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.common.action.ReduceStorageDomainCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.ReduceStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ReduceStorageDomainCommand<T extends ReduceStorageDomainCommandParameters> extends
        StorageJobCommand<T> {

    public ReduceStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        ReduceStorageDomainVDSCommandParameters p =
                new ReduceStorageDomainVDSCommandParameters(getParameters().getStorageJobId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getDeviceId());
        p.setVdsId(getParameters().getVdsRunningOn());
        vdsCommandsHelper.runVdsCommandWithoutFailover(VDSCommandType.ReduceStorageDomain,
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
