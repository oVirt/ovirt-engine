package org.ovirt.engine.core.bll.storage;

import java.util.Collections;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class AttachStorageDomainsMultipleActionRunner extends SortedMultipleActionsRunnerBase {
    public AttachStorageDomainsMultipleActionRunner(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters, boolean isInternal) {
        super(actionType, parameters, isInternal);
    }

    @Override
    public java.util.ArrayList<VdcReturnValueBase> Execute() {
        if (getParameters().size() > 0) {
            storage_pool pool = DbFacade.getInstance().getStoragePoolDAO().get(
                    ((StorageDomainPoolParametersBase) getParameters().get(0)).getStoragePoolId());
            if (pool.getstatus() == StoragePoolStatus.Uninitialized) {
                java.util.ArrayList<Guid> storageDomainIds = new java.util.ArrayList<Guid>();
                for (VdcActionParametersBase param : getParameters()) {
                    storageDomainIds.add(((StorageDomainPoolParametersBase) param).getStorageDomainId());
                }
                java.util.ArrayList<VdcActionParametersBase> parameters =
                        new java.util.ArrayList<VdcActionParametersBase>();
                parameters.add(new StoragePoolWithStoragesParameter(pool, storageDomainIds));
                if (isInternal) {
                    return Backend.getInstance().runInternalMultipleActions(VdcActionType.AddStoragePoolWithStorages,
                            parameters);
                } else {
                    return Backend.getInstance().RunMultipleActions(VdcActionType.AddStoragePoolWithStorages,
                            parameters);
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
            ThreadPoolUtil.execute(new Runnable() {

                @Override
                public void run() {
                    if (command.getReturnValue().getCanDoAction()) {
                        command.ExecuteAction();
                    }
                }
            });
        }
    }
}
