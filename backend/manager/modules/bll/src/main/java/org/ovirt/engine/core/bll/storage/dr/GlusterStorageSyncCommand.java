package org.ovirt.engine.core.bll.storage.dr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.action.GlusterStorageSyncCommandParameters;
import org.ovirt.engine.core.common.action.GlusterStorageSyncCommandParameters.DRStep;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;

@NonTransactiveCommandAttribute
public class GlusterStorageSyncCommand<T extends GlusterStorageSyncCommandParameters> extends CommandBase<T> implements SerialChildExecutingCommand {

    private static final String DR_SNAPSHOT_NAME_SUFFIX = "-TMPDR";

    @Inject
    private GlusterGeoRepDao geoRepDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private AuditLogDirector auditLogDirector;

    private GlusterGeoRepSession geoRepSession;

    public GlusterStorageSyncCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    private GlusterGeoRepSession getSession() {
        if (geoRepSession == null) {
            geoRepSession = geoRepDao.getById(getParameters().getGeoRepSessionId());
        }
        return geoRepSession;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_STORAGE_DOMAIN_SYNC,
                        new LockMessage(EngineMessage.ERROR_STORAGE_DOMAIN_SYNC_EXISTS)));
    }

    @Override
    protected void executeCommand() {
        // Get list of running VMs that have disks on storage domain
        List<VM> vms =
                runInternalQuery(QueryType.GetVmsByStorageDomain, new IdQueryParameters(getStorageDomain().getId()))
                        .getReturnValue();

        // Snapshot the VMs
        Map<Guid, Guid> vmIdSnapshotIdMap = new HashMap<>();
        for (VM vm : vms) {
            try {
                Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                        ActionType.CreateSnapshotForVm,
                        getCreateSnapshotParameters(vm),
                        cloneContextAndDetachFromParent());
                vmIdSnapshotIdMap.put(vm.getId(), future.get().getActionReturnValue());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error creating VM snapshot for VM with id '{}', name '{}' for DR sync, error: '{}'",
                        vm.getId(),
                        vm.getName(),
                        e.getMessage());
                log.debug("Exception", e);
                endWithFailure();
                getParameters().setTaskGroupSuccess(false);
            }
        }
        getParameters().setVmIdSnapshotIds(vmIdSnapshotIdMap);
        getParameters().setNextStep(DRStep.GEO_REP);
        persistCommandIfNeeded();
        setSucceeded(true);
    }

    @Override
    public List getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }

    @Override
    protected void endWithFailure() {
        getParameters().setTaskGroupSuccess(false);
        setSucceeded(false);
        removeDRSnapshots();
    }

    private void removeDRSnapshots() {
        for (Map.Entry<Guid, Guid> entry : getParameters().getVmIdSnapshotIds().entrySet()) {
            RemoveSnapshotParameters removeSnapshotParameters = new RemoveSnapshotParameters(entry.getValue(),
                    entry.getKey());
            removeSnapshotParameters.setParentCommand(getActionType());
            removeSnapshotParameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
            removeSnapshotParameters.setParentParameters(getParameters());
            removeSnapshotParameters.setNeedsLocking(false);

            commandCoordinatorUtil.executeAsyncCommand(ActionType.RemoveSnapshot,
                    removeSnapshotParameters,
                    cloneContextAndDetachFromParent());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return AuditLogType.GLUSTER_STORAGE_DOMAIN_SYNC_STARTED;
        case END_SUCCESS:
            return AuditLogType.GLUSTER_STORAGE_DOMAIN_SYNCED;
        default:
            return AuditLogType.GLUSTER_STORAGE_DOMAIN_SYNC_FAILED;
        }
    }

    protected CreateSnapshotForVmParameters getCreateSnapshotParameters(VM vm) {
        Set<Guid> diskIds =
                vm.getDiskList().stream()
                    .map(BaseDisk::getId)
                    .collect(Collectors.toSet());

        CreateSnapshotForVmParameters params = new CreateSnapshotForVmParameters(vm.getId(),
                vm.getName() + getStorageDomain().getName() + DR_SNAPSHOT_NAME_SUFFIX,
                false);

        params.setParentCommand(getActionType());
        params.setSnapshotType(SnapshotType.REGULAR);
        params.setParentParameters(getParameters());
        params.setDiskIds(diskIds);
        params.setNeedsLocking(false);
        params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return params;
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getNextStep() == null) {
            return false;
        }
        switch (getParameters().getNextStep()) {
        case GEO_REP:
            GlusterVolumeGeoRepSessionParameters parameters = getGeoRepParameters();
            getParameters().setNextStep(DRStep.REMOVE_TMP_SNAPSHOTS);
            ActionReturnValue vdcRetValue = runInternalActionWithTasksContext(ActionType.GlusterStorageGeoRepSyncInternal, parameters);
            if (!vdcRetValue.getSucceeded()) {
                auditLogDirector.log(this, AuditLogType.GLUSTER_VOLUME_GEO_REP_START_FAILED_EXCEPTION);
                endWithFailure();
            }
            persistCommandIfNeeded();
            break;
        case REMOVE_TMP_SNAPSHOTS:
            removeDRSnapshots();
            getParameters().setNextStep(null);
            persistCommandIfNeeded();
            break;
        }
        return true;
    }

    private GlusterVolumeGeoRepSessionParameters getGeoRepParameters() {
        GlusterVolumeGeoRepSessionParameters parameters =
                new GlusterVolumeGeoRepSessionParameters(getSession().getMasterVolumeId(),
                        getSession().getId());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        return parameters;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
