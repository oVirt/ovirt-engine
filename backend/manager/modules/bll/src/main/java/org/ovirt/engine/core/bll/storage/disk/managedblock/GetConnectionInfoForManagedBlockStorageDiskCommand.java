package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ConnectManagedBlockStorageDeviceCommandParameters;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockCommandParameters;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockExecutor;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.utils.JsonHelper;

@NonTransactiveCommandAttribute
public class GetConnectionInfoForManagedBlockStorageDiskCommand<T extends ConnectManagedBlockStorageDeviceCommandParameters> extends CommandBase<T> {

    @Inject
    private ManagedBlockExecutor managedBlockExecutor;

    @Inject
    private CinderStorageDao cinderStorageDao;

    public GetConnectionInfoForManagedBlockStorageDiskCommand(T parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public GetConnectionInfoForManagedBlockStorageDiskCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(getParameters().getStorageDomainId());
        List<String> extraParams = new ArrayList<>();
        extraParams.add(getParameters().getDiskId().toString());

        try {
            ManagedBlockCommandParameters params = new ManagedBlockCommandParameters(
                    JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(),
                            false),
                    extraParams,
                    getCorrelationId());
            ManagedBlockReturnValue returnValue =
                    managedBlockExecutor.runCommand(ManagedBlockExecutor.ManagedBlockCommand.GET_CONNECTION_INFO, params);
            if (!returnValue.getSucceed()) {
                return;
            }
            getReturnValue().setActionReturnValue(JsonHelper.jsonToMap(returnValue.getOutput()));
        } catch (Exception e) {
            log.error("Failed executing volume creation", e);
            return;
        }

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

}

