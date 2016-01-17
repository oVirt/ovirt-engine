package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.RefreshVolumeParameters;
import org.ovirt.engine.core.common.vdscommands.RefreshVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RefreshVolumeCommand<T extends RefreshVolumeParameters>
        extends CommandBase<T> {

    public RefreshVolumeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.RefreshVolume,
                createVDSParameters());

        if (vdsReturnValue != null && vdsReturnValue.getSucceeded()) {
            setSucceeded(true);
            log.info("Successfully refreshed volume '{}' on host '{}'",
                    getParameters().getImageId(), getParameters().getVdsId());
        } else {
            log.error("Failed to refresh volume '{}' on host '{}'",
                    getParameters().getImageId(), getParameters().getVdsId());
        }
    }

    private VDSParametersBase createVDSParameters() {
        return new RefreshVolumeVDSCommandParameters(
                getParameters().getVdsId(),
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                getParameters().getImageId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}
