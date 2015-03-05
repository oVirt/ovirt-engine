package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.action.gluster.SetUpMountBrokerParameters;
import org.ovirt.engine.core.common.action.gluster.UpdateGlusterHostPubKeyToSlaveParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class CreateGlusterVolumeGeoRepSessionCommand extends GlusterVolumeCommandBase<GlusterVolumeGeoRepSessionParameters> {

    private GlusterVolumeEntity slaveVolume;
    private Set<VDS> remoteServersSet;
    private VDS slaveHost;

    public CreateGlusterVolumeGeoRepSessionCommand(GlusterVolumeGeoRepSessionParameters params) {
        this(params, null);
    }

    public CreateGlusterVolumeGeoRepSessionCommand(GlusterVolumeGeoRepSessionParameters params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected boolean canDoAction() {
        if (!GlusterFeatureSupported.glusterGeoReplication(getVdsGroup().getCompatibilityVersion())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GEO_REP_NOT_SUPPORTED);
        }
        slaveHost = getSlaveHost();
        if (slaveHost == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
        }
        if (slaveHost.getStatus() != VDSStatus.Up) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP,
                    String.format("$%1$s %2$s", "VdsName", slaveHost.getName()));
        }
        slaveVolume = getSlaveVolume();
        if (slaveVolume == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
        }
        if (slaveVolume.getStatus() != GlusterStatus.UP) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SHOULD_BE_STARTED);
        }
        if (!areAllRemoteServersUp()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_ONE_OR_MORE_REMOTE_HOSTS_ARE_NOT_ACCESSIBLE);
        }
        GlusterGeoRepSession geoRepSession =
                getGeoRepDao().getGeoRepSession(getGlusterVolumeId(),
                        getParameters().getSlaveHost(),
                        getParameters().getSlaveVolumeName());
        if (geoRepSession != null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_GEOREP_SESSION_ALREADY_CREATED);
        }
        return super.canDoAction();
    }

    protected GlusterGeoRepDao getGeoRepDao() {
        return getDbFacade().getGlusterGeoRepDao();
    }

    protected GlusterVolumeEntity getSlaveVolume() {
        return getGlusterVolumeDao().getByName(slaveHost.getVdsGroupId(),
                getParameters().getSlaveVolumeName());
    }

    protected VDS getSlaveHost() {
        return getVdsDAO().getByName(getParameters().getSlaveHost());
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
        addCustomValue(GlusterConstants.GEO_REP_USER_GROUP, getParameters().getUserGroup());
        addCustomValue(GlusterConstants.GEO_REP_SLAVE_VOLUME_NAME, getParameters().getSlaveVolumeName());
        addCustomValue(GlusterConstants.SERVICE_TYPE, ServiceType.GLUSTER.name());
        return super.getCustomValues();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_GEOREP_SESSION);
    }

    @Override
    protected void executeCommand() {
        boolean rootSession = getParameters().getUserName().equalsIgnoreCase("root");
        boolean succeeded = true;
        if (!rootSession) {
            succeeded = setUpMountBrokerOnSlaves();
        }
        if (succeeded) {
            succeeded = setUpPasswordlessSSHAndCreateSession();
            if (succeeded) {
                GlusterGeoRepSyncJob.getInstance().refreshGeoRepDataForVolume(getGlusterVolume());
            }
        }
    }

    private boolean setUpMountBrokerOnSlaves() {
        List<Callable<Boolean>> mountBrokerSetupReturnStatuses = new ArrayList<>();
        for (final VDS currentSlaveServer : remoteServersSet) {
            mountBrokerSetupReturnStatuses.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return getBackend().runInternalAction(VdcActionType.SetupGlusterGeoRepMountBrokerInternal,
                            new SetUpMountBrokerParameters(currentSlaveServer.getId(),
                    getParameters().getSlaveVolumeName(),
                    getParameters().getUserName(),
                                    getParameters().getUserGroup())).getSucceeded();
                }
            });
        }
        List<Boolean> returnStatuses = ThreadPoolUtil.invokeAll(mountBrokerSetupReturnStatuses);
        for (Boolean currentReturnStatus : returnStatuses) {
            if (!currentReturnStatus) {
                return false;
            }
        }
        return true;
    }

    private boolean setUpPasswordlessSSHAndCreateSession() {
        List<String> pubKeys = readPubKey();
        boolean canProceed = pubKeys != null && pubKeys.size() > 0;
        if (canProceed) {
            canProceed = updatePubKeysToRemoteHosts(pubKeys);
            if (canProceed) {
                canProceed = createGeoRepSession();
            }
        }
        return canProceed;
    }

    private boolean updatePubKeysToRemoteHosts(final List<String> pubKeys) {
        List<Callable<Boolean>> slaveWritePubKeyList = new ArrayList<>();
        for (final VDS currentRemoteHost : remoteServersSet) {
            slaveWritePubKeyList.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return getBackend().runInternalAction(VdcActionType.UpdateGlusterHostPubKeyToSlaveInternal,
                            new UpdateGlusterHostPubKeyToSlaveParameters(currentRemoteHost.getId(),
                                    pubKeys, getParameters().getUserName())).getSucceeded();
                }
            });
        }
        List<Boolean> returnStatuses = ThreadPoolUtil.invokeAll(slaveWritePubKeyList);
        for (Boolean currentReturnStatus : returnStatuses) {
            if (!currentReturnStatus) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<String> readPubKey() {
        VdcQueryReturnValue readPubKeyReturnvalue =
                runInternalQuery(VdcQueryType.GetGlusterHostPublicKeys, new IdQueryParameters(upServer.getId()));
        if (readPubKeyReturnvalue.getSucceeded()) {
            return (List<String>) readPubKeyReturnvalue.getReturnValue();
        } else {
            handleVdsError(AuditLogType.GLUSTER_GEO_REP_PUB_KEY_FETCH_FAILED, readPubKeyReturnvalue.getExceptionString());
            return null;
        }
    }

    private boolean createGeoRepSession() {
        GlusterVolumeGeoRepSessionVDSParameters params =
                new GlusterVolumeGeoRepSessionVDSParameters(upServer.getId(),
                        getGlusterVolumeName(),
                        getParameters().getSlaveHost(),
                        getParameters().getSlaveVolumeName(),
                        getParameters().getUserName(),
                        getParameters().isForce());
        VDSReturnValue createSessionReturnValue =
                runVdsCommand(VDSCommandType.CreateGlusterVolumeGeoRepSession, params);
        if (createSessionReturnValue.getSucceeded()) {
            setSucceeded(true);
            return true;
        } else {
            setSucceeded(false);
            handleVdsError(AuditLogType.GLUSTER_GEOREP_SESSION_CREATE_FAILED, createSessionReturnValue.getVdsError()
                    .getMessage());
            return false;
        }
    }

    private Set<VDS> fetchRemoteServers() {
        Set<VDS> remoteServers = new HashSet<>();
        List<GlusterBrickEntity> slaveBricks = slaveVolume.getBricks();
        for (GlusterBrickEntity currentBrick : slaveBricks) {
            remoteServers.add(getVdsDAO().get(currentBrick.getServerId()));
        }
        return remoteServers;
    }
}
