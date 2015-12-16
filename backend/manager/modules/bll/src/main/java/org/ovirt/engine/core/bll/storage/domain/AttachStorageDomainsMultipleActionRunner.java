package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.SortedMultipleActionsRunnerBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class AttachStorageDomainsMultipleActionRunner extends SortedMultipleActionsRunnerBase {
    public AttachStorageDomainsMultipleActionRunner(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    public ArrayList<VdcReturnValueBase> execute() {
        Iterator<?> iterator = getParameters() == null ? null : getParameters().iterator();
        Object parameter = iterator != null && iterator.hasNext() ? iterator.next() : null;

        if (parameter instanceof StorageDomainPoolParametersBase) {
            StorageDomainPoolParametersBase storagePoolParameter = (StorageDomainPoolParametersBase) parameter;
            StoragePool pool = DbFacade.getInstance().getStoragePoolDao().get(storagePoolParameter.getStoragePoolId());
            if (pool.getStatus() == StoragePoolStatus.Uninitialized) {
                ArrayList<Guid> storageDomainIds = new ArrayList<>();
                for (VdcActionParametersBase param : getParameters()) {
                    storageDomainIds.add(((StorageDomainPoolParametersBase) param).getStorageDomainId());
                }
                ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
                parameters.add(new StoragePoolWithStoragesParameter(pool,
                        storageDomainIds,
                        storagePoolParameter.getSessionId()));
                if (isInternal) {
                    return Backend.getInstance().runInternalMultipleActions(VdcActionType.AddStoragePoolWithStorages,
                            parameters);
                } else {
                    return Backend.getInstance().runMultipleActions(VdcActionType.AddStoragePoolWithStorages,
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
