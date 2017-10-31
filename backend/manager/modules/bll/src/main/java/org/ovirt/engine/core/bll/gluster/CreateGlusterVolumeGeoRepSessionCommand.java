package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.action.gluster.SetUpMountBrokerParameters;
import org.ovirt.engine.core.common.action.gluster.SetUpPasswordLessSSHParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class CreateGlusterVolumeGeoRepSessionCommand extends GlusterVolumeCommandBase<GlusterVolumeGeoRepSessionParameters> {

    private GlusterVolumeEntity slaveVolume;
    private Set<VDS> remoteServersSet;
    private VDS slaveHost;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private GlusterGeoRepSyncJob glusterGeoRepSyncJob;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    public CreateGlusterVolumeGeoRepSessionCommand(GlusterVolumeGeoRepSessionParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected boolean validate() {
        slaveHost = getSlaveHost();
        if (slaveHost == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
        }
        if (slaveHost.getStatus() != VDSStatus.Up) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP,
                    String.format("$%1$s %2$s", "VdsName", slaveHost.getName()));
        }
        slaveVolume = getSlaveVolume();
        if (slaveVolume == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
        }
        if (slaveVolume.getStatus() != GlusterStatus.UP) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SHOULD_BE_STARTED);
        }
        if (!areAllRemoteServersUp()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_ONE_OR_MORE_REMOTE_HOSTS_ARE_NOT_ACCESSIBLE);
        }
        GlusterGeoRepSession geoRepSession =
                glusterGeoRepDao.getGeoRepSession(getGlusterVolumeId(),
                        slaveHost.getId(),
                        getParameters().getSlaveVolumeName());
        if (geoRepSession != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_GEOREP_SESSION_ALREADY_CREATED);
        }
        return super.validate();
    }

    protected GlusterVolumeEntity getSlaveVolume() {
        return glusterVolumeDao.getByName(slaveHost.getClusterId(), getParameters().getSlaveVolumeName());
    }

    protected VDS getSlaveHost() {
        return vdsDao.get(getParameters().getSlaveHostId());
    }

    private boolean areAllRemoteServersUp() {
        remoteServersSet = fetchRemoteServers();
        for (VDS currentRemoteServer : remoteServersSet) {
            if (currentRemoteServer.getStatus() != VDSStatus.Up) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, String> getCustomValues() {
        addCustomValue(GlusterConstants.VOLUME_NAME, getGlusterVolumeName());
        addCustomValue(GlusterConstants.GEO_REP_USER, getParameters().getUserName());
        addCustomValue(GlusterConstants.GEO_REP_SLAVE_VOLUME_NAME, getParameters().getSlaveVolumeName());
        addCustomValue(GlusterConstants.SERVICE_TYPE, ServiceType.GLUSTER.name());
        return super.getCustomValues();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_GEOREP_SESSION);
    }

    @Override
    protected void executeCommand() {
        boolean rootSession = getParameters().getUserName().equalsIgnoreCase("root");
        boolean succeeded = true;
        Set<Guid> remoteServerIds = getServerIds(remoteServersSet);
        Guid slaveHostId = getParameters().getSlaveHostId();
        if (!rootSession) {
            ActionReturnValue completeMountBrokerSetupOnSlaveInternalAction =
                    backend.runInternalAction(ActionType.SetupGlusterGeoRepMountBrokerInternal,
                            new SetUpMountBrokerParameters(vdsDao.get(slaveHostId).getClusterId(),
                                    new HashSet<>(Collections.singletonList(getParameters().getSlaveHostId())),
                                    getParameters().getSlaveVolumeName(),
                                    getParameters().getUserName(),
                                    getParameters().getUserGroup()));
            succeeded = evaluateReturnValue(AuditLogType.GLUSTER_GEOREP_SETUP_MOUNT_BROKER_FAILED, completeMountBrokerSetupOnSlaveInternalAction);
            remoteServerIds.remove(slaveHostId);
            if (succeeded) {
                auditLogDirector.log(this, AuditLogType.GLUSTER_SETUP_GEOREP_MOUNT_BROKER);
                if (!remoteServerIds.isEmpty()) {
                    ActionReturnValue mountBrokerPartialSetupInternalAction =
                            backend.runInternalAction(ActionType.SetupGlusterGeoRepMountBrokerInternal,
                                    new SetUpMountBrokerParameters(vdsDao.get(slaveHostId).getClusterId(),
                                            remoteServerIds,
                                            getParameters().getSlaveVolumeName(),
                                            getParameters().getUserName()));
                    succeeded =
                            evaluateReturnValue(AuditLogType.GLUSTER_GEOREP_SETUP_MOUNT_BROKER_FAILED,
                                    mountBrokerPartialSetupInternalAction);
                    if (succeeded) {
                        auditLogDirector.log(this, AuditLogType.GLUSTER_SETUP_GEOREP_MOUNT_BROKER);
                    }
                }
            }
        }
        if (succeeded) {
            remoteServerIds.add(slaveHostId);
            ActionReturnValue setUpPasswordLessSSHinternalAction =
                    runInternalAction(ActionType.SetUpPasswordLessSSHInternal,
                            new SetUpPasswordLessSSHParameters(upServer.getClusterId(),
                                    remoteServerIds,
                                    getParameters().getUserName()));
            succeeded = evaluateReturnValue(errorType, setUpPasswordLessSSHinternalAction);
            if (succeeded) {
                auditLogDirector.log(this, AuditLogType.SET_UP_PASSWORDLESS_SSH);
                VDSReturnValue createVdsReturnValue = runVdsCommand(VDSCommandType.CreateGlusterVolumeGeoRepSession,
                        new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(),
                                getGlusterVolumeName(),
                                vdsDao.get(slaveHostId).getHostName(),
                                getParameters().getSlaveVolumeName(),
                                getParameters().getUserName(),
                                getParameters().isForce()));
                succeeded = evaluateReturnValue(AuditLogType.GLUSTER_GEOREP_SESSION_CREATE_FAILED, createVdsReturnValue);
                if (succeeded) {
                    glusterGeoRepSyncJob.refreshGeoRepDataForVolume(getGlusterVolume());
                }
            }
        }
        setSucceeded(succeeded);
    }

    private Set<Guid> getServerIds(Set<VDS> remoteServersSet) {
        Set<Guid> remoteServerIds = new HashSet<>();
        for (VDS currentVds : remoteServersSet) {
            remoteServerIds.add(currentVds.getId());
        }
        return remoteServerIds;
    }

    private Set<VDS> fetchRemoteServers() {
        Set<VDS> remoteServers = new HashSet<>();
        List<GlusterBrickEntity> slaveBricks = slaveVolume.getBricks();
        for (GlusterBrickEntity currentBrick : slaveBricks) {
            remoteServers.add(vdsDao.get(currentBrick.getServerId()));
        }
        return remoteServers;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.CREATE_GLUSTER_VOLUME_GEOREP_SESSION : AuditLogType.GLUSTER_GEOREP_SESSION_CREATE_FAILED;
    }
}
