package org.ovirt.engine.core.bll;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class DestroyImageCheckCommand<T extends DestroyImageParameters>
        extends CommandBase<T> {
    public DestroyImageCheckCommand(T parameters) {
        super(parameters);
    }

    public DestroyImageCheckCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        List<Guid> failedGuids = getParameters().getImageList() == null
                ? Collections.emptyList()
                : getParameters().getImageList().stream()
                        .filter(this::volumeExists)
                        .collect(toList());

        if (failedGuids.isEmpty()) {
            log.info("Requested images were successfully removed");
            setSucceeded(true);
            persistCommand(getParameters().getParentCommand());
        } else {
            log.error("The following images were not removed: {}", failedGuids);
        }
    }

    private boolean volumeExists(Guid volumeId) {
        log.debug("Checking for the existence of volume '{0}' using GetVolumeInfo", volumeId);
        GetVolumeInfoVDSCommandParameters params = new GetVolumeInfoVDSCommandParameters(
                getParameters().getVdsId(),
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                volumeId);
        try {
            runVdsCommand(VDSCommandType.GetVolumeInfo, params);
        } catch (EngineException e) {
            if (e.getVdsError().getCode() == EngineError.VolumeDoesNotExist) {
                return false;
            }
            // We can't assume the volume is gone; return true so that Live Merge fails
            log.error("Failed to determine volume '{0}' existence using GetVolumeInfo", volumeId, e);
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}
