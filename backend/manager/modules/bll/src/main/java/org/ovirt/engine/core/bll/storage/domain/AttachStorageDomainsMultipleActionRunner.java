package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.SortedMultipleActionsRunnerBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class AttachStorageDomainsMultipleActionRunner extends SortedMultipleActionsRunnerBase {
    public AttachStorageDomainsMultipleActionRunner(ActionType actionType,
            List<ActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private BackendInternal backend;

    @Override
    public List<ActionReturnValue> execute() {
        Iterator<?> iterator = getParameters() == null ? null : getParameters().iterator();
        Object parameter = iterator != null && iterator.hasNext() ? iterator.next() : null;

        if (parameter instanceof StorageDomainPoolParametersBase) {
            StorageDomainPoolParametersBase storagePoolParameter = (StorageDomainPoolParametersBase) parameter;
            StoragePool pool = storagePoolDao.get(storagePoolParameter.getStoragePoolId());
            if (pool.getStatus() == StoragePoolStatus.Uninitialized) {
                List<Guid> storageDomainIds = new ArrayList<>();
                for (ActionParametersBase param : getParameters()) {
                    storageDomainIds.add(((StorageDomainPoolParametersBase) param).getStorageDomainId());
                }
                List<ActionParametersBase> parameters = new ArrayList<>();
                parameters.add(new StoragePoolWithStoragesParameter(pool,
                        storageDomainIds,
                        storagePoolParameter.getSessionId()));
                if (isInternal) {
                    return backend.runInternalMultipleActions(ActionType.AddStoragePoolWithStorages,
                            parameters);
                } else {
                    return backend.runMultipleActions(ActionType.AddStoragePoolWithStorages,
                            parameters, false);
                }
            } else {
                return super.execute();
            }
        } else {
            return super.execute();
        }
    }

    @Override
    protected void sortCommands() {
        Collections.sort(getCommands(), new StorageDomainsByTypeComparer());
    }

    @Override
    protected void runCommands() {
        sortCommands();

        for (final CommandBase<?> command : getCommands()) {
            if (command.getReturnValue().isValid()) {
                ThreadPoolUtil.execute(() -> executeValidatedCommand(command));
            }
        }
    }
}
