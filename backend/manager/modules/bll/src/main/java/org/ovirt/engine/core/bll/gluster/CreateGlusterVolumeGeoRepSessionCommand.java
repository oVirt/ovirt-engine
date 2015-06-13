package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
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
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
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
        if (!getGlusterUtil().isGlusterGeoReplicationSupported(getVdsGroup().getcompatibility_version(),
                getVdsGroup().getId())) {
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
                        slaveHost.getId(),
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
        return getVdsDAO().get(getParameters().getSlaveHostId());
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
        Set<Guid> remoteServerIds = getServerIds(remoteServersSet);
        Guid slaveHostId = getParameters().getSlaveHostId();
        if (!rootSession) {
            VdcReturnValueBase mountBrokerOnSlaveReturnValue = setUpMountBrokerOnSlave();
            succeeded =
                    evaluateReturnValues(AuditLogType.GLUSTER_GEOREP_SETUP_MOUNT_BROKER_FAILED,
                            Collections.singletonList(mountBrokerOnSlaveReturnValue));
            remoteServerIds.remove(slaveHostId);
            if (succeeded) {
                succeeded =
                        evaluateReturnValues(AuditLogType.GLUSTER_GEOREP_SETUP_MOUNT_BROKER_FAILED,
                                setUpPartialMountBrokerOnSlaves(remoteServerIds));
            }
        }
        if (succeeded) {
            remoteServerIds.add(slaveHostId);
            succeeded = setUpPasswordlessSSH(upServer.getId(), remoteServerIds, getParameters().getUserName());
            if (succeeded) {
                succeeded =
                        createGeoRepSession(upServer.getId(),
                                getGlusterVolumeName(),
                                getVdsDAO().get(slaveHostId).getHostName(),
                                getParameters().getSlaveVolumeName(),
                                getParameters().getUserName(),
                                getParameters().isForce(),
                                true);
                if (succeeded) {
                    GlusterGeoRepSyncJob.getInstance().refreshGeoRepDataForVolume(getGlusterVolume());
                }
            }
        }
    }

    private Set<Guid> getServerIds(Set<VDS> remoteServersSet) {
        Set<Guid> remoteServerIds = new HashSet<Guid>();
        for (VDS currentVds : remoteServersSet) {
            remoteServerIds.add(currentVds.getId());
        }
        return remoteServerIds;
    }

    protected VdcReturnValueBase setUpMountBrokerOnSlave() {
        VdcReturnValueBase mountBrokerOnSlaveReturnValue = getBackend().runInternalAction(VdcActionType.SetupGlusterGeoRepMountBrokerInternal,
                new SetUpMountBrokerParameters(getParameters().getSlaveHostId(),
                        getParameters().getSlaveVolumeName(),
                        getParameters().getUserName(),
                        getParameters().getUserGroup()));
        return mountBrokerOnSlaveReturnValue;
    }

    protected List<VdcReturnValueBase> setUpPartialMountBrokerOnSlaves(Set<Guid> remoteServerIds) {
        if (remoteServerIds == null || remoteServerIds.isEmpty()) {
            return null;
        }
        List<Callable<VdcReturnValueBase>> mountBrokerPartialSetupReturnStatuses = new ArrayList<Callable<VdcReturnValueBase>>();
        for (final Guid currentSlaveServerId : remoteServerIds) {
            mountBrokerPartialSetupReturnStatuses.add(new Callable<VdcReturnValueBase>() {
                @Override
                public VdcReturnValueBase call() throws Exception {
                    return getBackend().runInternalAction(VdcActionType.SetupGlusterGeoRepMountBrokerInternal,
                            new SetUpMountBrokerParameters(currentSlaveServerId,
                                    getParameters().getSlaveVolumeName(),
                                    getParameters().getUserName(),
                                    getParameters().getUserGroup(),
                                    true));
                }
            });
        }
        List<VdcReturnValueBase> returnStatuses = ThreadPoolUtil.invokeAll(mountBrokerPartialSetupReturnStatuses);
        return returnStatuses;
    }

    protected boolean setUpPasswordlessSSH(Guid masterUpServerId, Set<Guid> remoteServerSet, String userName) {
        List<String> pubKeys = readPubKey(masterUpServerId);
        boolean canProceed = pubKeys != null && pubKeys.size() > 0;
        if (canProceed) {
            canProceed = evaluateReturnValues(AuditLogType.GLUSTER_GEOREP_PUBLIC_KEY_WRITE_FAILED, updatePubKeysToRemoteHosts(pubKeys, remoteServerSet, userName));
        }
        return canProceed;
    }

    private List<VdcReturnValueBase> updatePubKeysToRemoteHosts(final List<String> pubKeys,
            Set<Guid> remoteServersSet,
            final String userName) {
        List<Callable<VdcReturnValueBase>> slaveWritePubKeyList = new ArrayList<Callable<VdcReturnValueBase>>();
        for (final Guid currentRemoteHostId : remoteServersSet) {
            slaveWritePubKeyList.add(new Callable<VdcReturnValueBase>() {
                @Override
                public VdcReturnValueBase call() throws Exception {
                    return getBackend().runInternalAction(VdcActionType.UpdateGlusterHostPubKeyToSlaveInternal,
                            new UpdateGlusterHostPubKeyToSlaveParameters(currentRemoteHostId,
                                    pubKeys, userName));
                }
            });
        }
        List<VdcReturnValueBase> returnStatuses = ThreadPoolUtil.invokeAll(slaveWritePubKeyList);
        return returnStatuses;
    }

    @SuppressWarnings("unchecked")
    private List<String> readPubKey(Guid upServerId) {
        VdcQueryReturnValue readPubKeyReturnvalue =
                runInternalQuery(VdcQueryType.GetGlusterHostPublicKeys, new IdQueryParameters(upServerId));
        if (readPubKeyReturnvalue.getSucceeded()) {
            return (List<String>) readPubKeyReturnvalue.getReturnValue();
        } else {
            handleVdsError(AuditLogType.GLUSTER_GEO_REP_PUB_KEY_FETCH_FAILED,
                    readPubKeyReturnvalue.getExceptionString());
            return null;
        }
    }

    protected boolean createGeoRepSession(Guid upServerId,
            String masterVolumeName,
            String remoteHost,
            String remoteVolumeName,
            String userName,
            Boolean force,
            Boolean handleError) {
        GlusterVolumeGeoRepSessionVDSParameters params =
                new GlusterVolumeGeoRepSessionVDSParameters(upServerId,
                        masterVolumeName,
                        remoteHost,
                        remoteVolumeName,
                        userName,
                        force);
        VDSReturnValue createSessionReturnValue =
                runVdsCommand(VDSCommandType.CreateGlusterVolumeGeoRepSession, params);
        if (createSessionReturnValue.getSucceeded()) {
            setSucceeded(true);
            return true;
        } else {
            setSucceeded(false);
            if (handleError) {
                handleVdsError(AuditLogType.GLUSTER_GEOREP_SESSION_CREATE_FAILED,
                        createSessionReturnValue.getVdsError().getMessage());
            }
            return false;
        }
    }

    private Set<VDS> fetchRemoteServers() {
        Set<VDS> remoteServers = new HashSet<VDS>();
        List<GlusterBrickEntity> slaveBricks = slaveVolume.getBricks();
        for (GlusterBrickEntity currentBrick : slaveBricks) {
            remoteServers.add(getVdsDAO().get(currentBrick.getServerId()));
        }
        return remoteServers;
    }

    protected GlusterUtil getGlusterUtil() {
        return GlusterUtil.getInstance();
    }

    private boolean evaluateReturnValues(AuditLogType auditLogType, List<VdcReturnValueBase> returnValues) {
        boolean succeeded = true;
        List<String> errors = new ArrayList<>();
        for (VdcReturnValueBase currentReturnValue : returnValues) {
            boolean currentExecutionStatus = currentReturnValue.getSucceeded();
            succeeded = succeeded && currentExecutionStatus;
            if (!currentExecutionStatus) {
                errors.addAll(currentReturnValue.getExecuteFailedMessages());
            }
        }
        if (!succeeded) {
            handleVdsErrors(auditLogType, errors);
        }
        return succeeded;
    }
}
