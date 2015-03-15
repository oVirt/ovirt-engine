package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public abstract class BaseIscsiBondCommand<T extends VdcActionParametersBase> extends CommandBase<T> {

    protected boolean encounterConnectionProblems;

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
                    try {
                        final List<StorageServerConnections> conns = ISCSIStorageHelper.updateIfaces(connections, host.getId());
                        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.ConnectStorageServer,
                                new StorageServerConnectionManagementVDSParameters(host.getId(), Guid.Empty, StorageType.ISCSI, conns)
                        );
                        final Map<String, String> iscsiMap = (Map<String, String>) returnValue.getReturnValue();
                        List<String> failedConnectionsList = LinqUtils.filter(iscsiMap.keySet(), new Predicate<String>() {
                            @Override
                            public boolean eval(String a) {
                                return !"0".equals(iscsiMap.get(a));
                            }
                        });
                        if (!failedConnectionsList.isEmpty()) {
                            log.error("Host '{}' - '{}' encounter problems to connect to the iSCSI Storage"
                                            + " Server. The following connections were problematic"+ "" +
                                            " (connectionid=vdsm result): {}",
                                    host.getName(),
                                    host.getId(),
                                    iscsiMap.toString());
                            encounterConnectionProblems = true;
                        }
                    } catch (VdcBLLException e) {
                        log.error("Could not connect Host '{}' - '{}' to Iscsi Storage Server: {}",
                                host.getName(),
                                host.getId(),
                                e.getMessage());
                        log.debug("Exception", e);
                        encounterConnectionProblems = true;
                    }
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
