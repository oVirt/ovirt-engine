package org.ovirt.engine.core.bll.storage.dr;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionConfigParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.di.Injector;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class GlusterStorageGeoRepSyncInternalCommand<T extends GlusterVolumeGeoRepSessionParameters> extends CommandBase<T> {

    @Inject
    private GlusterGeoRepDao geoRepDao;

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
            Future<VdcReturnValueBase> geoRepCmd =
                    CommandCoordinatorUtil.executeAsyncCommand(VdcActionType.StartGlusterVolumeGeoRep,
                            new GlusterVolumeGeoRepSessionParameters(getSession().getMasterVolumeId(),
                                    getSession().getId()),
                            cloneContext());
            VdcReturnValueBase result;
            try {
                result = geoRepCmd.get();
                if (!result.getSucceeded()) {
                    propagateFailure(result);
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Exception", e);
                return;
            }
        }

        // checkpoint the replication session
        GlusterVolumeGeoRepSessionConfigParameters configParams =
                new GlusterVolumeGeoRepSessionConfigParameters(getSession().getMasterVolumeId(),
                        getSession().getId(),
                        GlusterConstants.GEOREP_CHECKPOINT_OPTION,
                        GlusterConstants.GEOREP_CHECKPOINT_VALUE);
        VdcReturnValueBase result = runInternalAction(VdcActionType.SetGeoRepConfig, configParams);
        if (!result.getSucceeded()) {
            propagateFailure(result);
            return;
        }
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public CommandCallback getCallback() {
        return Injector.injectMembers(new GlusterStorageGeoRepSyncCallback());
    }
}
