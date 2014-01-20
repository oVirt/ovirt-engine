package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public abstract class BaseIscsiBondCommand<T extends VdcActionParametersBase> extends CommandBase<T> {

    public BaseIscsiBondCommand(T parameters) {
        super(parameters);
    }

    public BaseIscsiBondCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }

    protected void connectAllHostsToStorage(List<String> connectionIds) {
        List<Callable<Void>> tasks = new ArrayList<>();
        final List<StorageServerConnections> connections = getDbFacade().getStorageServerConnectionDao().getByIds(connectionIds);
        List<VDS> hosts = getVdsDAO().getAllForStoragePoolAndStatus(getIscsiBond().getStoragePoolId(), VDSStatus.Up);

        for (final VDS host : hosts) {
            tasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final List<StorageServerConnections> conns = ISCSIStorageHelper.updateIfaces(connections, host.getId());
                    runVdsCommand(VDSCommandType.ConnectStorageServer,
                            new StorageServerConnectionManagementVDSParameters(host.getId(), Guid.Empty, StorageType.ISCSI, conns)
                    );
                    return null;
                }
            });
        }

        ThreadPoolUtil.invokeAll(tasks);
    }

    /**
     * Used by audit log to populate ${IscsiBondName} placeholder.
     * @return
     */
    public String getIscsiBondName() {
        return getIscsiBond().getName();
    }

    protected abstract IscsiBond getIscsiBond();
}
