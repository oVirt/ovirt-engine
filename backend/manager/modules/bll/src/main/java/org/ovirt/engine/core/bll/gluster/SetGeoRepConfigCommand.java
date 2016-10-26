package org.ovirt.engine.core.bll.gluster;

import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionConfigParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepConfigVdsParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeOptionVDSParameters;

public class SetGeoRepConfigCommand extends GeoRepSessionCommandBase<GlusterVolumeGeoRepSessionConfigParameters> {

    public SetGeoRepConfigCommand(GlusterVolumeGeoRepSessionConfigParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected void init() {
        super.init();
        setGlusterVolumeId(getGeoRepSession().getMasterVolumeId());
    }

    @Override
    public Map<String, String> getCustomValues() {
        addCustomValue(GlusterConstants.OPTION_KEY, getParameters().getConfigKey());
        addCustomValue(GlusterConstants.OPTION_VALUE, getParameters().getConfigValue());
        addCustomValue(GlusterConstants.GEO_REP_SESSION_KEY, getGeoRepSession().getSessionKey());
        return super.getCustomValues();
    }

    @Override
    protected void executeCommand() {
        GlusterGeoRepSession session = getGeoRepSession();
        String configKey = getParameters().getConfigKey();
        String configValue = getParameters().getConfigValue();
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.SetGlusterVolumeGeoRepConfig,
                        new GlusterVolumeGeoRepConfigVdsParameters(upServer.getId(),
                                session.getMasterVolumeName(),
                                session.getSlaveHostName(),
                                session.getSlaveVolumeName(),
                                configKey,
                                configValue,
                                session.getUserName()));
        boolean succeeded = returnValue.getSucceeded();
        if (succeeded && configKey.equals("use_meta_volume")) {
            // Not handling failures as there's no way to figure out if the error is that the option is already set.
            runVdsCommand(VDSCommandType.SetGlusterVolumeOption,
                    new GlusterVolumeOptionVDSParameters(upServer.getId(), "all", new GlusterVolumeOptionEntity(
                            getGeoRepSession().getMasterVolumeId(), "cluster.enable-shared-storage", "enable")));
        }
        setSucceeded(succeeded);
        if (getSucceeded()) {
            GlusterGeoRepSessionConfiguration geoRepSessionConfig = new GlusterGeoRepSessionConfiguration();
            geoRepSessionConfig.setValue(configValue);
            geoRepSessionConfig.setKey(configKey);
            geoRepSessionConfig.setId(session.getId());
            if(glusterGeoRepDao.getGeoRepSessionConfigByKey(session.getId(), configKey) == null) {
                glusterGeoRepDao.saveConfig(geoRepSessionConfig);
            } else {
                glusterGeoRepDao.updateConfig(geoRepSessionConfig);
            }
        } else {
            handleVdsError(AuditLogType.GLUSTER_GEOREP_CONFIG_SET_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SET);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_GEOREP_CONFIG);
        addValidationMessageVariable("configName", getParameters().getConfigKey());
        addValidationMessageVariable("geoRepSessionKey", getGeoRepSession().getSessionKey());
        addValidationMessageVariable("configValue", getParameters().getConfigValue());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_GEOREP_CONFIG_SET;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_GEOREP_CONFIG_SET_FAILED : errorType;
        }
    }
}
