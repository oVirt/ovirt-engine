package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.dao.StorageDomainDRDao;

/**
 * BLL command to stop a geo-replication session
 */
@NonTransactiveCommandAttribute
public class DeleteGeoRepSessionCommand extends GeoRepSessionCommandBase<GlusterVolumeGeoRepSessionParameters> {

    @Inject
    private StorageDomainDRDao storageDomainDRDao;

    public DeleteGeoRepSessionCommand(GlusterVolumeGeoRepSessionParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withNoWait();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_GEOREP_SESSION);
        addValidationMessageVariable("volumeName", getGlusterVolumeName());
        addValidationMessageVariable("cluster", getClusterName());
    }

    @Override
    protected boolean validate() {
        if (!storageDomainDRDao.getWithGeoRepSession(getParameters().getGeoRepSessionId()).isEmpty()) {
            //cannot delete this session as there's an storage domain sync setup against this.
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GEOREP_SESSION_USED_IN_STORAGE_SYNC);
        }
        return super.validate();
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.DeleteGlusterVolumeGeoRepSession,
                        new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(),
                                getGeoRepSession().getMasterVolumeName(), getGeoRepSession().getSlaveHostName(),
                                getGeoRepSession().getSlaveVolumeName(), getGeoRepSession().getUserName()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            glusterGeoRepDao.remove(getGeoRepSession().getId());
        } else {
            handleVdsError(AuditLogType.GEOREP_SESSION_DELETE_FAILED, returnValue.getVdsError().getMessage());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GEOREP_SESSION_DELETED;
        } else {
            return errorType == null ? AuditLogType.GEOREP_SESSION_DELETE_FAILED : errorType;
        }
    }

}
