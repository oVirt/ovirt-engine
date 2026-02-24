package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.SaveManagedBlockStorageDiskDeviceCommandParameters;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockCommandParameters;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockExecutor;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ManagedBlockStorageDao;
import org.ovirt.engine.core.utils.JsonHelper;

public class SaveManagedBlockStorageDiskDeviceCommand<T extends SaveManagedBlockStorageDiskDeviceCommandParameters> extends CommandBase<T> {

    @Inject
    private ManagedBlockStorageDao managedBlockStorageDao;

    @Inject
    private ManagedBlockExecutor managedBlockExecutor;

    public SaveManagedBlockStorageDiskDeviceCommand(T parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public SaveManagedBlockStorageDiskDeviceCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        boolean succeeded;
        ManagedBlockStorage managedBlockStorage = managedBlockStorageDao.get(getParameters().getStorageDomainId());

        try {
            List<String> extraParams = new ArrayList<>();
            extraParams.add(getParameters().getDiskId().toString());
            extraParams.add(JsonHelper.mapToJson(getParameters().getDevice(), false));

            ManagedBlockCommandParameters params = new ManagedBlockCommandParameters(
                    JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(),
                            false),
                    extraParams,
                    getCorrelationId());
            succeeded = managedBlockExecutor
                    .runCommand(ManagedBlockExecutor.ManagedBlockCommand.SAVE_DEVICE, params)
                    .getSucceed();
        } catch (Exception e) {
            log.error("Failed executing save_device verb", e);
            setSucceeded(false);
            return;
        }

        setSucceeded(succeeded);

    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }
}
