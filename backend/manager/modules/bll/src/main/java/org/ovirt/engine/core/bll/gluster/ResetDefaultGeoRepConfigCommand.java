package org.ovirt.engine.core.bll.gluster;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionConfigParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepConfigVdsParameters;

public class ResetDefaultGeoRepConfigCommand extends GeoRepSessionCommandBase<GlusterVolumeGeoRepSessionConfigParameters> {

    @Inject
    private GlusterGeoRepSyncJob glusterGeoRepSyncJob;

    public ResetDefaultGeoRepConfigCommand(GlusterVolumeGeoRepSessionConfigParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected void init() {
        super.init();
        setGlusterVolumeId(getGeoRepSession().getMasterVolumeId());
    }

    @Override
    protected void executeCommand() {
        GlusterGeoRepSession session = getGeoRepSession();
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.SetGlusterVolumeGeoRepConfigDefault,
                        new GlusterVolumeGeoRepConfigVdsParameters(upServer.getId(),
                                session.getMasterVolumeName(),
                                session.getSlaveHostName(),
                                session.getSlaveVolumeName(),
                                getParameters().getConfigKey(),
                                null,
                                session.getUserName()));
        glusterGeoRepSyncJob.updateDiscoveredSessionConfig(getCluster(), session);
        setSucceeded(returnValue.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_GEOREP_CONFIG_SET_DEFAULT_FAILED, returnValue.getVdsError()
                    .getMessage());
            return;
        }
    }

    @Override
    public Map<String, String> getCustomValues() {
        addCustomValue(GlusterConstants.OPTION_KEY, getParameters().getConfigKey());
        addCustomValue(GlusterConstants.GEO_REP_SESSION_KEY, getGeoRepSession().getSessionKey());
        return super.getCustomValues();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__RESET);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_GEOREP_CONFIG);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_GEOREP_CONFIG_SET_DEFAULT;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_GEOREP_CONFIG_SET_DEFAULT_FAILED : errorType;
        }
    }
}
