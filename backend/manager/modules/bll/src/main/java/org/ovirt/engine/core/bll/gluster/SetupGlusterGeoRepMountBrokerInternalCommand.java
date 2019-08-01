package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.SetUpMountBrokerParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.SetUpGlusterGeoRepMountBrokerVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class SetupGlusterGeoRepMountBrokerInternalCommand extends GlusterCommandBase<SetUpMountBrokerParameters> {

    @Inject
    private VdsDao vdsDao;
    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    GlusterVolumeEntity slaveVolume;

    public SetupGlusterGeoRepMountBrokerInternalCommand(SetUpMountBrokerParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    protected GlusterVolumeEntity getSlaveVolume() {
        return glusterVolumeDao.getByName(getParameters().getId(), getParameters().getRemoteVolumeName());
    }

    @Override
    protected boolean validate() {
        slaveVolume =
                getSlaveVolume();
        if (slaveVolume != null) {
            setGlusterVolumeId(slaveVolume.getId());
            setClusterId(slaveVolume.getClusterId());
        } else {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
        }
        if (slaveVolume.getStatus() != GlusterStatus.UP) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SHOULD_BE_STARTED);
        }
        return super.validate();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SETUP);
        addValidationMessage(EngineMessage.VAR__TYPE__GEOREP_MOUNT_BROKER);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_SETUP_GEOREP_MOUNT_BROKER;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_GEOREP_SETUP_MOUNT_BROKER_FAILED : errorType;
        }
    }

    @Override
    public Map<String, String> getCustomValues() {
        addCustomValue(GlusterConstants.GEO_REP_USER, getParameters().getRemoteUserName());
        addCustomValue(GlusterConstants.GEO_REP_USER_GROUP, getParameters().getRemoteUserGroup());
        addCustomValue(GlusterConstants.GEO_REP_SLAVE_VOLUME_NAME, getParameters().getRemoteVolumeName());
        addCustomValue(GlusterConstants.SERVICE_TYPE, ServiceType.GLUSTER.name());
        return super.getCustomValues();
    }

    private VDSReturnValue restartGlusterd(Guid serverId) {
        getCustomValues().put(GlusterConstants.VDS_NAME, vdsDao.get(serverId).getName());
        GlusterServiceVDSParameters params =
                new GlusterServiceVDSParameters(serverId,
                        Collections.singletonList("glusterd"),
                        GlusterConstants.MANAGE_GLUSTER_SERVICE_ACTION_TYPE_RESTART);
        return runVdsCommand(VDSCommandType.ManageGlusterService, params);
    }

    @Override
    protected void executeCommand() {
        boolean succeeded = true;
        final SetUpMountBrokerParameters parameters = getParameters();
        final List<Callable<VDSReturnValue>> mountBrokerSetupReturnStatuses = new ArrayList<>();
        for (final Guid currentRemoteServerId : getParameters().getRemoteServerIds()) {
            mountBrokerSetupReturnStatuses.add(() -> setUpMountBrokerPartial(currentRemoteServerId,
                    parameters.getRemoteUserName(),
                    parameters.getRemoteUserGroup(),
                    parameters.getRemoteVolumeName(),
                    parameters.isPartial()));
        }
        List<VDSReturnValue> returnValues = ThreadPoolUtil.invokeAll(mountBrokerSetupReturnStatuses);
        List<String> errors = new ArrayList<>();
        for (VDSReturnValue currentReturnValue : returnValues) {
            if (!currentReturnValue.getSucceeded()) {
                succeeded = false;
                errors.add(currentReturnValue.getVdsError().getMessage());
            }
        }
        if (!errors.isEmpty()) {
            propagateFailure(AuditLogType.GLUSTER_GEOREP_SETUP_MOUNT_BROKER_FAILED, errors);
        }
        setSucceeded(succeeded);
    }

    private VDSReturnValue setUpMountBrokerPartial(Guid currentHostId,
            String remoteUserName,
            String remoteUserGroupName,
            String remoteVolumeName,
            boolean partial) {
        VDSReturnValue mountBrokerReturnValue =
                runVdsCommand(VDSCommandType.SetupGlusterGeoRepMountBroker,
                        new SetUpGlusterGeoRepMountBrokerVDSParameters(currentHostId,
                                remoteUserName,
                                remoteUserGroupName,
                                remoteVolumeName,
                                partial));
        if (mountBrokerReturnValue.getSucceeded()) {
            return restartGlusterd(currentHostId);
        }
        return mountBrokerReturnValue;
    }
}
