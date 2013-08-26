package org.ovirt.engine.core.bll.storage;

import java.util.Collections;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.SortedMultipleActionsRunnerBase;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class AttachStorageDomainsMultipleActionRunner extends SortedMultipleActionsRunnerBase {
    public AttachStorageDomainsMultipleActionRunner(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters, boolean isInternal) {
        super(actionType, parameters, isInternal);
    }

    @Override
    public java.util.ArrayList<VdcReturnValueBase> Execute() {
        if (getParameters().size() > 0) {
            StoragePool pool = DbFacade.getInstance().getStoragePoolDao().get(
                    ((StorageDomainPoolParametersBase) getParameters().get(0)).getStoragePoolId());
            if (pool.getStatus() == StoragePoolStatus.Uninitialized) {
                java.util.ArrayList<Guid> storageDomainIds = new java.util.ArrayList<Guid>();
                for (VdcActionParametersBase param : getParameters()) {
                    storageDomainIds.add(((StorageDomainPoolParametersBase) param).getStorageDomainId());
                }
                java.util.ArrayList<VdcActionParametersBase> parameters =
                        new java.util.ArrayList<VdcActionParametersBase>();
                parameters.add(new StoragePoolWithStoragesParameter(pool, storageDomainIds, getParameters().get(0)
                        .getSessionId()));
                if (isInternal) {
                    return Backend.getInstance().runInternalMultipleActions(VdcActionType.AddStoragePoolWithStorages,
                            parameters);
                } else {
                    return Backend.getInstance().RunMultipleActions(VdcActionType.AddStoragePoolWithStorages,
                            parameters, false);
                }
            } else {
                return super.Execute();
            }
        } else {
            return super.Execute();
        }
    }

    @Override
    protected void SortCommands() {
        Collections.sort(getCommands(), new StorageDomainsByTypeComparer());
    }

    @Override
    protected void RunCommands() {
        SortCommands();

        for (final CommandBase<?> command : getCommands()) {
            if (command.getReturnValue().getCanDoAction()) {
                ThreadPoolUtil.execute(new Runnable() {

                    @Override
                    public void run() {
                        executeValidatedCommand(command);
                    }
                });
            }
        }
    }
}
