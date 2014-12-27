package org.ovirt.engine.core.bll.gluster;

import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionConfigParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepConfigVdsParameters;

public class ResetDefaultGeoRepConfigCommand extends GeoRepSessionCommandBase<GlusterVolumeGeoRepSessionConfigParameters> {

    public ResetDefaultGeoRepConfigCommand(GlusterVolumeGeoRepSessionConfigParameters params) {
        super(params);
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
                                getParameters().getConfigKey()));
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
    protected boolean canDoAction() {
        return super.canDoAction();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RESET);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_GEOREP_CONFIG);
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
