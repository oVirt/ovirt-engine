package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.GlusterAuditLogUtil;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.action.gluster.SetUpMountBrokerParameters;
import org.ovirt.engine.core.common.action.gluster.SetUpPasswordLessSSHParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeBricksActionVDSParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AddBricksToGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeBricksActionParameters> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private GlusterAuditLogUtil logUtil;
    @Inject
    private GlusterVolumeDao glusterVolumeDao;
    @Inject
    private GlusterUtil glusterUtil;
    @Inject
    private GlusterBrickDao glusterBrickDao;

    public AddBricksToGlusterVolumeCommand(GlusterVolumeBricksActionParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_BRICK);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getParameters().getBricks() == null || getParameters().getBricks().size() == 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
        }

        if (getGlusterVolume().getVolumeType().isReplicatedType()) {
            if (getParameters().getReplicaCount() > getGlusterVolume().getReplicaCount() + 1) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_REPLICA_COUNT_MORE_THAN_ONE);
            } else if (getParameters().getReplicaCount() > getGlusterVolume().getReplicaCount()
                    && getGlusterVolume().getIsArbiter()) {
                return failValidation(
                        EngineMessage.ACTION_TYPE_FAILED_GLUSTER_ARBITER_VOLUME_SHOULD_BE_REPLICA_3_VOLUME);
            } else if (getParameters().getReplicaCount() < getGlusterVolume().getReplicaCount()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_REPLICA_COUNT);
            }
        }
        if (getGlusterVolume().getVolumeType().isStripedType()) {
            if (getParameters().getStripeCount() > getGlusterVolume().getStripeCount() + 1) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_INCREASE_STRIPE_COUNT_MORE_THAN_ONE);
            } else if (getParameters().getStripeCount() < getGlusterVolume().getStripeCount()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CAN_NOT_REDUCE_STRIPE_COUNT);
            }
        }
        if (getGlusterVolume().getVolumeType().isDispersedType()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_ADD_BRICK_TO_DISPERSE_VOLUME_NOT_SUPPORTED);
        }

        boolean ret = updateBrickServerAndInterfaceNames(getParameters().getBricks(), true)
                && validateDuplicateBricks(getParameters().getBricks());
        //only validate bricks are not from same server for HC clusters.
        if (getCluster().supportsGlusterService() && getCluster().supportsVirtService() &&
                getGlusterVolume().getVolumeType().isReplicatedType()) {
            ret = ret && validateNotSameServer(getParameters().getBricks(),
                                               getGlusterVolume(),
                                               getParameters().getReplicaCount());
        }
        return ret;
    }

    @Override
    protected void executeCommand() {
        final List<GlusterBrickEntity> bricksList = getParameters().getBricks();
        GlusterVolumeEntity volumeBeforeBrickAdd = getGlusterVolume();

        // Add bricks in a single transaction
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                () -> {
                    addGlusterVolumeBricks(bricksList,
                            getParameters().getReplicaCount(),
                            getParameters().getStripeCount(),
                            getParameters().isForce());
                    return null;
                });
        if (getGlusterVolume().getIsGeoRepMaster() || getGlusterVolume().getIsGeoRepSlave()) {
            Set<Guid> newServerIds = findNewServers(bricksList, volumeBeforeBrickAdd);
            if (!newServerIds.isEmpty()) {
                postAddBrickHandleGeoRepCase(bricksList, newServerIds);
            }
        }
    }

    private void postAddBrickHandleGeoRepCase(final List<GlusterBrickEntity> bricksList, final Set<Guid> newServerIds) {
        // newServerIds is the set of ids of the servers that were not part of the volume before this attempt of brick
        // addition.
        final GlusterVolumeEntity volume = getGlusterVolume();
        List<GlusterGeoRepSession> sessions = new ArrayList<>();

        // Get all sessions for which the volume is a master
        List<GlusterGeoRepSession> geoRepSessionsForVolumeAsMaster = glusterGeoRepDao.getGeoRepSessions(volume.getId());
        if (geoRepSessionsForVolumeAsMaster != null && !geoRepSessionsForVolumeAsMaster.isEmpty()) {
            sessions.addAll(geoRepSessionsForVolumeAsMaster);
        }
        // Get session for which the volume is a slave
        GlusterGeoRepSession geoRepSessionForVolumeAsSlave =
                glusterGeoRepDao.getGeoRepSessionBySlaveVolume(volume.getId());
        if (geoRepSessionForVolumeAsSlave != null) {
            sessions.add(geoRepSessionForVolumeAsSlave);
        }

        // If this volume is empty, nothing to do.
        if (sessions.isEmpty()) {
            return;
        }

        List<Callable<Boolean>> perSessionCallables = new ArrayList<>();
        for (final GlusterGeoRepSession currentSession : sessions) {
            perSessionCallables.add(() -> {
                // Ids of servers on which steps like mount broker setup and/or passwordless ssh need to be done.
                Set<Guid> serverIdsToPrep = new HashSet<>(newServerIds);
                // Assume current volume as master volume of current session
                GlusterVolumeEntity masterVolume = volume;
                boolean succeeded = true;
                addCustomValue(GlusterConstants.VOLUME_NAME, currentSession.getMasterVolumeName());
                addCustomValue(GlusterConstants.GEO_REP_SLAVE_VOLUME_NAME, currentSession.getSlaveVolumeName());
                addCustomValue(GlusterConstants.GEO_REP_USER, currentSession.getUserName());
                if (currentSession.getMasterVolumeId().equals(volume.getId())) {
                    /*
                     * If the volume is master, and there are any new servers, serverIdsToPrep is a set of all slave
                     * servers. This is bcoz the new server's keys also need to be updated to all slave servers.
                     */
                    serverIdsToPrep = getSlaveNodesSet(currentSession);
                } else {
                    // If its slave and non-root session, do partial mount broker setup
                    if (!currentSession.getUserName().equalsIgnoreCase("root")) {
                        succeeded =
                                evaluateReturnValue(errorType,
                                        backend.runInternalAction(ActionType.SetupGlusterGeoRepMountBrokerInternal,
                                                new SetUpMountBrokerParameters(volume.getClusterId(),
                                                        serverIdsToPrep,
                                                        volume.getName(),
                                                        currentSession.getUserName())));
                        if (succeeded) {
                            auditLogDirector.log(this,
                                    AuditLogType.GLUSTER_SETUP_GEOREP_MOUNT_BROKER);
                        }
                    }
                    /*
                     * If the assumption that current volume is master, is invalid, which will be known here, update
                     * master volume correctly.
                     */
                    masterVolume = glusterVolumeDao.getById(currentSession.getMasterVolumeId());
                }
                if (succeeded) {
                    succeeded =
                            evaluateReturnValue(errorType,
                                    runInternalAction(ActionType.SetUpPasswordLessSSHInternal,
                                            new SetUpPasswordLessSSHParameters(masterVolume.getClusterId(),
                                                    serverIdsToPrep,
                                                    currentSession.getUserName())));
                }
                if (succeeded) {
                    auditLogDirector.log(this, AuditLogType.SET_UP_PASSWORDLESS_SSH);
                    succeeded =
                            evaluateReturnValue(errorType,
                                    runVdsCommand(VDSCommandType.CreateGlusterVolumeGeoRepSession,
                                            new GlusterVolumeGeoRepSessionVDSParameters(glusterUtil.getRandomUpServer(masterVolume.getClusterId())
                                                    .getId(),
                                                    currentSession.getMasterVolumeName(),
                                                    currentSession.getSlaveHostName(),
                                                    currentSession.getSlaveVolumeName(),
                                                    currentSession.getUserName(),
                                                    true)));
                }
                if (currentSession.getStatus() == GeoRepSessionStatus.ACTIVE
                        || currentSession.getStatus() == GeoRepSessionStatus.INITIALIZING) {
                    succeeded =
                            evaluateReturnValue(errorType,
                                    runInternalAction(ActionType.StartGlusterVolumeGeoRep,
                                            new GlusterVolumeGeoRepSessionParameters(currentSession.getMasterVolumeId(),
                                                    currentSession.getId(),
                                                    true)));
                }
                return succeeded;
            });
        }
        ThreadPoolUtil.invokeAll(perSessionCallables);
    }

    private Set<Guid> getSlaveNodesSet(GlusterGeoRepSession currentSession) {
        Set<Guid> slaveNodesSet = new HashSet<>();
        GlusterVolumeEntity sessionSlaveVolume = glusterVolumeDao.getById(currentSession.getSlaveVolumeId());
        for (GlusterBrickEntity currentSlaveBrick : sessionSlaveVolume.getBricks()) {
            slaveNodesSet.add(currentSlaveBrick.getServerId());
        }
        return slaveNodesSet;
    }

    private Set<Guid> findNewServers(final List<GlusterBrickEntity> bricksList, GlusterVolumeEntity volumeBeforeBrickAdd) {
        final Set<Guid> newServerIds = new HashSet<>();
        for (GlusterBrickEntity currentBrick : bricksList) {
            if (isNewServer(currentBrick.getServerId(), volumeBeforeBrickAdd)) {
                newServerIds.add(currentBrick.getServerId());
            }
        }
        return newServerIds;
    }

    private boolean isNewServer(Guid serverId, GlusterVolumeEntity volumeBeforeBrickAdd) {
        List<GlusterBrickEntity> bricks = volumeBeforeBrickAdd.getBricks();
        for (GlusterBrickEntity currentBrick : bricks) {
            if (currentBrick.getServerId().equals(serverId)) {
                return false;
            }
        }
        return true;
    }

    private void addGlusterVolumeBricks(List<GlusterBrickEntity> bricksList,
            int replicaCount,
            int stripeCount,
            boolean force) {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.AddBricksToGlusterVolume,
                                new GlusterVolumeBricksActionVDSParameters(upServer.getId(),
                                        getGlusterVolumeName(),
                                        bricksList,
                                        replicaCount,
                                        stripeCount,
                                        upServer.getClusterCompatibilityVersion(),
                                        force));

        setSucceeded(returnValue.getSucceeded());

        if (getSucceeded()) {
            addCustomValue(GlusterConstants.NO_OF_BRICKS, String.valueOf(bricksList.size()));
            addGlusterVolumeBricksInDb(bricksList, replicaCount, stripeCount);
            logAuditMessages(bricksList);
            getReturnValue().setActionReturnValue(getBrickIds(bricksList));
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    private void logAuditMessages(List<GlusterBrickEntity> bricks) {
        for (final GlusterBrickEntity brick : bricks) {
            logUtil.logAuditMessage(null,
                    null,
                    null,
                    null,
                    AuditLogType.GLUSTER_VOLUME_BRICK_ADDED, new HashMap<String, String>() {
                        {
                            put(GlusterConstants.BRICK_PATH, brick.getBrickDirectory());
                            put(GlusterConstants.SERVER_NAME, brick.getServerName());
                            put(GlusterConstants.VOLUME_NAME, getGlusterVolumeName());
                        }
                    });
        }
    }

    private List<Guid> getBrickIds(List<GlusterBrickEntity> bricks) {
        List<Guid> brickIds = new ArrayList<>();
        for (GlusterBrickEntity brick : bricks) {
            brickIds.add(brick.getId());
        }
        return brickIds;
    }

    private void addGlusterVolumeBricksInDb(List<GlusterBrickEntity> newBricks, int replicaCount, int stripeCount) {
        // Reorder the volume bricks
        GlusterVolumeEntity volume = getGlusterVolume();
        List<GlusterBrickEntity> volumeBricks = volume.getBricks();
        if (isReplicaCountIncreased(replicaCount) || isStripeCountIncreased(stripeCount)) {
            GlusterBrickEntity brick;
            int brick_num = 0;
            int count =
                    isReplicaCountIncreased(replicaCount) ? replicaCount : stripeCount;

            // Updating existing brick order
            for (int i = 0; i < volumeBricks.size(); i++) {
                if (((i + 1) % count) == 0) {
                    brick_num++;
                }
                brick = volumeBricks.get(i);
                brick.setBrickOrder(brick_num);
                brick_num++;

                glusterBrickDao.updateBrickOrder(brick.getId(), brick.getBrickOrder());
            }
            // Adding new bricks
            for (int i = 0; i < newBricks.size(); i++) {
                brick = newBricks.get(i);
                brick.setBrickOrder((i + 1) * count - 1);
                brick.setStatus(getBrickStatus());
                glusterBrickDao.save(brick);
            }

        } else {
            // No change in the replica/stripe count
            int brickCount = volumeBricks.get(volumeBricks.size() - 1).getBrickOrder();

            //If the volume is an arbiter volume then make every third brick as arbiter brick.
            if(volume.getIsArbiter()){
                for(int i=2;i<newBricks.size();i+=3){
                    newBricks.get(i).setIsArbiter(true);
                }
            }
            for (GlusterBrickEntity brick : newBricks) {
                brick.setBrickOrder(++brickCount);
                brick.setStatus(getBrickStatus());
                glusterBrickDao.save(brick);
            }
        }

        // Update the volume replica/stripe count
        if (isReplicaCountIncreased(replicaCount)) {
            volume.setReplicaCount(replicaCount);
        }

        if (volume.getVolumeType() == GlusterVolumeType.REPLICATE
                && replicaCount < (volume.getBricks().size() + newBricks.size())) {
            volume.setVolumeType(GlusterVolumeType.DISTRIBUTED_REPLICATE);
        }

        if (isStripeCountIncreased(stripeCount)) {
            volume.setStripeCount(stripeCount);
        }

        if (volume.getVolumeType() == GlusterVolumeType.STRIPE
                && stripeCount < (volume.getBricks().size() + newBricks.size())) {
            volume.setVolumeType(GlusterVolumeType.DISTRIBUTED_STRIPE);
        }
        //TODO: check for DISTRIBUTED_STRIPED_REPLICATE and STRIPED_REPLICATE

        glusterVolumeDao.updateGlusterVolume(volume);
    }

    private boolean isReplicaCountIncreased(int replicaCount) {
        return (getGlusterVolume().getVolumeType() == GlusterVolumeType.REPLICATE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE)
                && replicaCount > getGlusterVolume().getReplicaCount();
    }

    private boolean isStripeCountIncreased(int stripeCount) {
        return (getGlusterVolume().getVolumeType() == GlusterVolumeType.STRIPE
                || getGlusterVolume().getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE)
                && stripeCount > getGlusterVolume().getStripeCount();
    }

    private GlusterStatus getBrickStatus() {
        return getGlusterVolume().getStatus();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_ADD_BRICK;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED : errorType;
        }
    }
}
