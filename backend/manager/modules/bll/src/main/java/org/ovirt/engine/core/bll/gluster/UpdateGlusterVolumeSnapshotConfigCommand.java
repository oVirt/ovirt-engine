package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.UpdateGlusterVolumeSnapshotConfigParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotSetConfigVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotConfigDao;

public class UpdateGlusterVolumeSnapshotConfigCommand extends GlusterCommandBase<UpdateGlusterVolumeSnapshotConfigParameters> {

    private boolean updatesSuccessful;
    private List<String> failedCfgs;

    @Inject
    private GlusterVolumeSnapshotConfigDao glusterVolumeSnapshotConfigDao;

    public UpdateGlusterVolumeSnapshotConfigCommand(UpdateGlusterVolumeSnapshotConfigParameters params,
            CommandContext commandContext) {
        super(params, commandContext);
        setClusterId(params.getClusterId());
        failedCfgs = new ArrayList<>();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME_SNAPSHOT_CONFIG);
        addValidationMessage(EngineMessage.VAR__ACTION__VOLUME_SNAPSHOT_CONFIG_UPDATE);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getParameters().getClusterId() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID);
            return false;
        }

        if (getParameters().getConfigParams() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SNAPSHOT_CONFIG_PARAMS_IS_EMPTY);
            return false;
        }

        for (GlusterVolumeSnapshotConfig param : getParameters().getConfigParams()) {
            if (StringUtils.isEmpty(param.getParamValue())) {
                addCustomValue("snapshotConfigParam", param.getParamName());
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SNAPSHOT_CONFIG_PARAM_VALUE_IS_EMPTY);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        Guid clusterId = getParameters().getClusterId();
        Guid volumeId = getParameters().getVolumeId();
        List<GlusterVolumeSnapshotConfig> fetchedConfigParams =
                glusterVolumeSnapshotConfigDao.getConfigByClusterId(clusterId);

        // segregate the fetched cluster and volume specific config params
        Map<String, GlusterVolumeSnapshotConfig> fetchedClusterConfigParams = new HashMap<>();
        Map<String, GlusterVolumeSnapshotConfig> fetchedVolumeConfigParams = new HashMap<>();
        for (GlusterVolumeSnapshotConfig param : fetchedConfigParams) {
            if (Guid.isNullOrEmpty(param.getVolumeId())) {
                fetchedClusterConfigParams.put(param.getParamName(), param);
            } else if (volumeId != null && param.getVolumeId().equals(volumeId)) {
                fetchedVolumeConfigParams.put(param.getParamName(), param);
            }
        }

        List<GlusterVolumeSnapshotConfig> configParams = getParameters().getConfigParams();

        // segregate the cluster and volume specific config params
        Map<String, GlusterVolumeSnapshotConfig> clusterConfigParams = new HashMap<>();
        Map<String, GlusterVolumeSnapshotConfig> volumeConfigParams = new HashMap<>();
        for (GlusterVolumeSnapshotConfig param : configParams) {
            if (Guid.isNullOrEmpty(param.getVolumeId())) {
                clusterConfigParams.put(param.getParamName(), param);
            } else {
                volumeConfigParams.put(param.getParamName(), param);
            }
        }

        // form the final list of updated config params
        List<GlusterVolumeSnapshotConfig> updatedClusterConfigParams = new ArrayList<>();
        for (GlusterVolumeSnapshotConfig cfgParam : clusterConfigParams.values()) {
            GlusterVolumeSnapshotConfig fetchedCfgParam = fetchedClusterConfigParams.get(cfgParam.getParamName());
            if (fetchedCfgParam != null && !fetchedCfgParam.getParamValue().equals(cfgParam.getParamValue())) {
                updatedClusterConfigParams.add(cfgParam);
            }
        }
        List<GlusterVolumeSnapshotConfig> updatedVolumeConfigParams = new ArrayList<>();
        for (GlusterVolumeSnapshotConfig cfgParam : volumeConfigParams.values()) {
            GlusterVolumeSnapshotConfig fetchedCfgParam = fetchedVolumeConfigParams.get(cfgParam.getParamName());
            if (fetchedCfgParam != null && !fetchedCfgParam.getParamValue().equals(cfgParam.getParamValue())) {
                updatedVolumeConfigParams.add(cfgParam);
            }
        }

        List<GlusterVolumeSnapshotConfig> updatedConfigs = new ArrayList<>();
        for (GlusterVolumeSnapshotConfig param : updatedClusterConfigParams) {
            updatedConfigs.add(param);
        }
        for (GlusterVolumeSnapshotConfig param : updatedVolumeConfigParams) {
            updatedConfigs.add(param);
        }

        for (GlusterVolumeSnapshotConfig config : updatedConfigs) {
            VDSReturnValue retVal = runVdsCommand(VDSCommandType.SetGlusterVolumeSnapshotConfig,
                    new GlusterVolumeSnapshotSetConfigVDSParameters(upServer.getId(), config));

            if (!retVal.getSucceeded()) {
                failedCfgs.add(config.getParamName());
                updatesSuccessful = false;
            } else {
                if (config.getVolumeId() != null) {
                    glusterVolumeSnapshotConfigDao.updateConfigByVolumeIdAndName(config.getClusterId(),
                            config.getVolumeId(),
                            config.getParamName(),
                            config.getParamValue());
                } else {
                    glusterVolumeSnapshotConfigDao.updateConfigByClusterIdAndName(config.getClusterId(),
                            config.getParamName(),
                            config.getParamValue());
                }
                updatesSuccessful = true;
            }
        }

        if (!updatesSuccessful) {
            addCustomValue("failedSnapshotConfigs", failedCfgs.toString());
        }

        setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        List<GlusterVolumeSnapshotConfig> configParams = getParameters().getConfigParams();
        if (configParams.isEmpty()) {
            return null;
        }
        Guid clusterId = configParams.get(0).getClusterId();
        if (!isInternalExecution()) {
            return Collections.singletonMap(clusterId.toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_SNAPSHOT,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            if (updatesSuccessful) {
                return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CONFIG_UPDATED;
            } else {
                return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CONFIG_UPDATE_FAILED_PARTIALLY;
            }
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CONFIG_UPDATE_FAILED : errorType;
        }
    }
}
