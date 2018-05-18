package org.ovirt.engine.core.bll.storage.dr;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionConfigParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class GlusterStorageGeoRepSyncInternalCommand<T extends GlusterVolumeGeoRepSessionParameters> extends CommandBase<T> {

    @Inject
    private GlusterGeoRepDao geoRepDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(GlusterStorageGeoRepSyncCallback.class)
    private Instance<GlusterStorageGeoRepSyncCallback> callbackProvider;

    private GlusterGeoRepSession geoRepSession;

    public GlusterStorageGeoRepSyncInternalCommand(Guid commandId) {
        super(commandId);
    }

    public GlusterStorageGeoRepSyncInternalCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private GlusterGeoRepSession getSession() {
        if (geoRepSession == null) {
            geoRepSession = geoRepDao.getById(getParameters().getGeoRepSessionId());
        }
        return geoRepSession;
    }

    @Override
    protected void executeCommand() {
        if (getSession().getStatus() != GeoRepSessionStatus.ACTIVE) {
            // Start geo-replication
            Future<ActionReturnValue> geoRepCmd =
                    commandCoordinatorUtil.executeAsyncCommand(ActionType.StartGlusterVolumeGeoRep,
                            new GlusterVolumeGeoRepSessionParameters(getSession().getMasterVolumeId(),
                                    getSession().getId()),
                            cloneContext());
            ActionReturnValue result;
            try {
                result = geoRepCmd.get();
                if (!result.getSucceeded()) {
                    handleFailure(result);
                    throw new EngineException(EngineError.GlusterVolumeGeoRepSessionStartFailed, "Failed to start geo-replication session!");
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Exception", e);
                handleFailure(null);
                throw new EngineException(EngineError.GlusterVolumeGeoRepSessionStartFailed, "Failed to start geo-replication session!");
            }
        }

        // checkpoint the replication session
        GlusterVolumeGeoRepSessionConfigParameters configParams =
                new GlusterVolumeGeoRepSessionConfigParameters(getSession().getMasterVolumeId(),
                        getSession().getId(),
                        GlusterConstants.GEOREP_CHECKPOINT_OPTION,
                        GlusterConstants.GEOREP_CHECKPOINT_VALUE);
        ActionReturnValue result = runInternalAction(ActionType.SetGeoRepConfig, configParams);
        if (!result.getSucceeded()) {
            handleFailure(result);
            return;
        }
        setSucceeded(true);
    }

    private void handleFailure(ActionReturnValue result) {
        if (result != null) {
            propagateFailure(result);
        }
        setSucceeded(false);
        commandCoordinatorUtil.removeAllCommandsInHierarchy(getCommandId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
