package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.DisconnectManagedBlockStorageDeviceParameters;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.utils.JsonHelper;

@NonTransactiveCommandAttribute
public class DisconnectManagedBlockStorageDeviceCommand<T extends DisconnectManagedBlockStorageDeviceParameters> extends CommandBase<T> {

    @Inject
    private CinderlibExecutor cinderlibExecutor;

    @Inject
    private CinderStorageDao cinderStorageDao;

    public DisconnectManagedBlockStorageDeviceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public DisconnectManagedBlockStorageDeviceCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {

        boolean succeeded;
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(getParameters().getStorageDomainId());
        List<String> extraParams = new ArrayList<>();
        extraParams.add(getParameters().getDiskId().toString());

        try {
            CinderlibCommandParameters params =
                    new CinderlibCommandParameters(JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(),
                            false),
                            extraParams);

            succeeded = cinderlibExecutor
                        .runCommand(CinderlibExecutor.CinderlibCommand.DISCONNECT_VOLUME, params)
                        .getSucceed();

        } catch (Exception e) {
            log.error("Failed executing disconnect_volume verb", e);
            return;
        }

        setSucceeded(succeeded);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }
}
