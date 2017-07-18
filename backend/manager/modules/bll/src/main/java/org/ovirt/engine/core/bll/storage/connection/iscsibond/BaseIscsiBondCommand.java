package org.ovirt.engine.core.bll.storage.connection.iscsibond;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.ISCSIStorageHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public abstract class BaseIscsiBondCommand<T extends ActionParametersBase> extends CommandBase<T> {

    @Inject
    private ISCSIStorageHelper iscsiStorageHelper;
    @Inject
    protected IscsiBondDao iscsiBondDao;

    protected boolean encounterConnectionProblems;

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private VdsDao vdsDao;

    public BaseIscsiBondCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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
        final List<StorageServerConnections> connections = storageServerConnectionDao.getByIds(connectionIds);
        List<VDS> hosts = vdsDao.getAllForStoragePoolAndStatus(getIscsiBond().getStoragePoolId(), VDSStatus.Up);

        for (final VDS host : hosts) {
            tasks.add(() -> {
                try {
                    final List<StorageServerConnections> conns = iscsiStorageHelper.updateIfaces(connections, host.getId());
                    VDSReturnValue returnValue = runVdsCommand(VDSCommandType.ConnectStorageServer,
                            new StorageServerConnectionManagementVDSParameters(host.getId(), Guid.Empty, StorageType.ISCSI, conns)
                    );
                    final Map<String, String> iscsiMap = (Map<String, String>) returnValue.getReturnValue();
                    List<String> failedConnectionsList = iscsiMap.entrySet().stream()
                            .filter(e -> !"0".equals(e.getValue())).map(Map.Entry::getKey)
                            .collect(Collectors.toList());
                    if (!failedConnectionsList.isEmpty()) {
                        log.error("Host '{}' - '{}' encounter problems to connect to the iSCSI Storage"
                                        + " Server. The following connections were problematic"+ "" +
                                        " (connectionid=vdsm result): {}",
                                host.getName(),
                                host.getId(),
                                iscsiMap.toString());
                        encounterConnectionProblems = true;
                    }
                } catch (EngineException e) {
                    log.error("Could not connect Host '{}' - '{}' to Iscsi Storage Server: {}",
                            host.getName(),
                            host.getId(),
                            e.getMessage());
                    log.debug("Exception", e);
                    encounterConnectionProblems = true;
                }
                return null;
            });
        }

        ThreadPoolUtil.invokeAll(tasks);
    }

    /**
     * Used by audit log to populate ${IscsiBondName} placeholder.
     */
    public String getIscsiBondName() {
        return getIscsiBond().getName();
    }

    protected abstract IscsiBond getIscsiBond();
}
