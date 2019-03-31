package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ConnectManagedBlockStorageDeviceCommandParameters;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.utils.JsonHelper;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ConnectManagedBlockStorageDeviceCommand<T extends ConnectManagedBlockStorageDeviceCommandParameters> extends CommandBase<T> {

    @Inject
    private CinderlibExecutor cinderlibExecutor;

    @Inject
    private CinderStorageDao cinderStorageDao;

    public ConnectManagedBlockStorageDeviceCommand(T parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public ConnectManagedBlockStorageDeviceCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(getParameters().getStorageDomainId());

        try {
            List<String> extraParams = new ArrayList<>();
            extraParams.add(getParameters().getDiskId().toString());
            extraParams.add(JsonHelper.mapToJson(getParameters().getConnectorInfo(), false));

            CinderlibCommandParameters params =
                    new CinderlibCommandParameters(JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(),
                            false),
                            extraParams,
                            getCorrelationId());
            CinderlibReturnValue returnValue =
                    cinderlibExecutor.runCommand(CinderlibExecutor.CinderlibCommand.CONNECT_VOLUME, params);
            if (!returnValue.getSucceed()) {
                return;
            }
            getReturnValue().setActionReturnValue(JsonHelper.jsonToMap(returnValue.getOutput()));
        } catch (Exception e) {
            log.error("Failed executing volume connection", e);
            return;
        }

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }
}
