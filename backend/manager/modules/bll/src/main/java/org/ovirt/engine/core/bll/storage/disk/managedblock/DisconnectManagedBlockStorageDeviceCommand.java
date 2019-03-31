package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.DisconnectManagedBlockStorageDeviceParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.common.vdscommands.AttachManagedBlockStorageVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.JsonHelper;

@NonTransactiveCommandAttribute
public class DisconnectManagedBlockStorageDeviceCommand<T extends DisconnectManagedBlockStorageDeviceParameters> extends CommandBase<T> {

    @Inject
    private CinderlibExecutor cinderlibExecutor;

    @Inject
    private CinderStorageDao cinderStorageDao;

    @Inject
    private VdsDao vdsDao;

    public DisconnectManagedBlockStorageDeviceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public DisconnectManagedBlockStorageDeviceCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        if (!detachVolume().getSucceeded()) {
            return;
        }

        boolean succeeded;
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(getParameters().getStorageDomainId());
        List<String> extraParams = new ArrayList<>();
        extraParams.add(getParameters().getDiskId().toString());

        try {
            CinderlibCommandParameters params =
                    new CinderlibCommandParameters(JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(),
                            false),
                            extraParams,
                            getCorrelationId());

            succeeded = cinderlibExecutor
                        .runCommand(CinderlibExecutor.CinderlibCommand.DISCONNECT_VOLUME, params)
                        .getSucceed();
        } catch (Exception e) {
            log.error("Failed executing disconnect_volume verb", e);
            return;
        }

        setSucceeded(succeeded);
    }

    private VDSReturnValue detachVolume() {
        VDS vds = vdsDao.get(getParameters().getVdsId());
        AttachManagedBlockStorageVolumeVDSCommandParameters params =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(vds);
        params.setVolumeId(getParameters().getDiskId());
        return runVdsCommand(VDSCommandType.DetachManagedBlockStorageVolume, params);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }
}
