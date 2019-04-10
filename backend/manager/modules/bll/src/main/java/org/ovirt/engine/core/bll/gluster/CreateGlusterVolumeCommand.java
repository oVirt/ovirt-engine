package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.gluster.CreateReplicatedVolume;
import org.ovirt.engine.core.common.validation.group.gluster.CreateStripedVolume;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.CreateGlusterVolumeVDSParameters;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

/**
 * BLL command to create a new Gluster Volume
 */
@NonTransactiveCommandAttribute
public class CreateGlusterVolumeCommand extends GlusterCommandBase<CreateGlusterVolumeParameters> {

    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    public CreateGlusterVolumeCommand(CreateGlusterVolumeParameters params, CommandContext commandContext) {
        super(params, commandContext);
        if (getVolume() != null) {
            setClusterId(getVolume().getClusterId());
        }
    }

    private GlusterVolumeEntity getVolume() {
        return getParameters().getVolume();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            if (getVolume() != null) {
                jobProperties.put(GlusterConstants.VOLUME, getVolume().getName());
            }
        }

        return jobProperties;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {

        addValidationGroup(CreateEntity.class);
        if (getVolume().getVolumeType().isReplicatedType()) {
            addValidationGroup(CreateReplicatedVolume.class);
        }
        if (getVolume().getVolumeType().isStripedType()) {
            addValidationGroup(CreateStripedVolume.class);
        }
        return super.getValidationGroups();
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        Cluster cluster = getCluster();
        if (cluster == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID);
            return false;
        }

        if (!cluster.supportsGlusterService()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_DOES_NOT_SUPPORT_GLUSTER);
            return false;
        }

        if (getVolume().getVolumeType().isDispersedType()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CREATION_OF_DISPERSE_VOLUME_NOT_SUPPORTED);
            return false;
        }

        if (volumeNameExists(getVolume().getName())) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_NAME_ALREADY_EXISTS);
            addValidationMessageVariable("volumeName", getVolume().getName());
            return false;
        }

        if (getVolume().getIsArbiter()){
            if(!getVolume().getVolumeType().isReplicatedType() || getVolume().getReplicaCount() != 3){
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_ARBITER_VOLUME_SHOULD_BE_REPLICA_3_VOLUME);
            }
        }
        return validateBricks(getVolume());
    }

    private boolean volumeNameExists(String volumeName) {
        return glusterVolumeDao.getByName(getClusterId(), volumeName) != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.bll.CommandBase#executeCommand()
     */
    @Override
    protected void executeCommand() {
        // set the gluster volume name for audit purpose
        setGlusterVolumeName(getVolume().getName());

        if(getVolume().getTransportTypes() == null || getVolume().getTransportTypes().isEmpty()) {
            getVolume().addTransportType(TransportType.TCP);
        }

        // GLUSTER access protocol is enabled by default
        getVolume().addAccessProtocol(AccessProtocol.GLUSTER);
        if (!getVolume().getAccessProtocols().contains(AccessProtocol.NFS)) {
            getVolume().disableNFS();
        }

        if (getVolume().getAccessProtocols().contains(AccessProtocol.CIFS)) {
            getVolume().enableCifs();
        }

        VDSReturnValue returnValue = runVdsCommand(
                VDSCommandType.CreateGlusterVolume,
                        new CreateGlusterVolumeVDSParameters(upServer.getId(),
                                getVolume(),
                                upServer.getClusterCompatibilityVersion(),
                                getParameters().isForce()));
        setSucceeded(returnValue.getSucceeded());

        if(!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_CREATE_FAILED, returnValue.getVdsError().getMessage());
            return;
        }

        // Volume created successfully. Insert it to database.
        GlusterVolumeEntity createdVolume = (GlusterVolumeEntity) returnValue.getReturnValue();
        setVolumeType(createdVolume);
        setBrickOrder(createdVolume.getBricks());
        if(createdVolume.getIsArbiter()){
            setArbiterFlag(createdVolume);
        }
        addVolumeToDb(createdVolume);

        // If we log successful volume creation at the end of this command,
        // the messages from SetGlusterVolumeOptionCommand appear first,
        // making it look like options were set before volume was created.
        // Hence we explicitly log the volume creation before setting the options.
        auditLogDirector.log(this, AuditLogType.GLUSTER_VOLUME_CREATE);
        // And don't log it at the end
        setCommandShouldBeLogged(false);

        // set all options of the volume
        setVolumeOptions(createdVolume);

        getReturnValue().setActionReturnValue(createdVolume.getId());
    }

    /**
     * Sets every third brick as arbiter brick if GlusterVolume is an arbiter volume
     */
    private void setArbiterFlag(GlusterVolumeEntity volume) {
       for(int i=2;i<volume.getBricks().size();i+=3){
           volume.getBricks().get(i).setIsArbiter(volume.getIsArbiter());
       }
    }

    private void setVolumeType(GlusterVolumeEntity createdVolume) {
        if (createdVolume.getVolumeType() == GlusterVolumeType.REPLICATE &&
                createdVolume.getBricks().size() > createdVolume.getReplicaCount()) {
            createdVolume.setVolumeType(GlusterVolumeType.DISTRIBUTED_REPLICATE);
        } else if (createdVolume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE &&
                createdVolume.getBricks().size() == createdVolume.getReplicaCount()) {
            createdVolume.setVolumeType(GlusterVolumeType.REPLICATE);
        } else if (createdVolume.getVolumeType() == GlusterVolumeType.STRIPE &&
                createdVolume.getBricks().size() > createdVolume.getStripeCount()) {
            createdVolume.setVolumeType(GlusterVolumeType.DISTRIBUTED_STRIPE);
        } else if (createdVolume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE &&
                createdVolume.getBricks().size() == createdVolume.getStripeCount()) {
            createdVolume.setVolumeType(GlusterVolumeType.STRIPE);
        } else if (createdVolume.getVolumeType() == GlusterVolumeType.STRIPED_REPLICATE &&
                createdVolume.getBricks().size() > createdVolume.getReplicaCount() * createdVolume.getStripeCount()) {
            createdVolume.setVolumeType(GlusterVolumeType.DISTRIBUTED_STRIPED_REPLICATE);
        } else if (createdVolume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPED_REPLICATE &&
                createdVolume.getBricks().size() == createdVolume.getReplicaCount() * createdVolume.getStripeCount()) {
            createdVolume.setVolumeType(GlusterVolumeType.STRIPED_REPLICATE);
        }
    }

    /**
     * Sets all options of a volume by invoking the action {@link ActionType#SetGlusterVolumeOption} in a loop. <br>
     * Errors if any are collected and added to "executeFailedMessages"
     */
    private void setVolumeOptions(GlusterVolumeEntity volume) {
        List<String> errors = new ArrayList<>();
        for (GlusterVolumeOptionEntity option : volume.getOptions()) {
            // make sure that volume id is set
            option.setVolumeId(volume.getId());

            ActionReturnValue setOptionReturnValue =
                    runInternalAction(
                            ActionType.SetGlusterVolumeOption,
                            new GlusterVolumeOptionParameters(option),
                            createCommandContext(volume, option));
            if (!setOptionReturnValue.getSucceeded()) {
                setSucceeded(false);
                errors.addAll(setOptionReturnValue.getValidationMessages());
                errors.addAll(setOptionReturnValue.getExecuteFailedMessages());
            }
        }

        if (!errors.isEmpty()) {
            handleVdsErrors(AuditLogType.GLUSTER_VOLUME_OPTION_SET_FAILED, errors);
        }
    }

    /**
     * Creates command context for setting a given option on the given volume
     */
    private CommandContext createCommandContext(GlusterVolumeEntity volume, GlusterVolumeOptionEntity option) {
        // Add sub-step for setting given option
        Step setOptionStep = addSubStep(StepEnum.EXECUTING,
                StepEnum.SETTING_GLUSTER_OPTION, getOptionValues(volume, option));

        // Create execution context for setting option
        ExecutionContext setOptionCtx = new ExecutionContext();
        setOptionCtx.setMonitored(true);
        setOptionCtx.setStep(setOptionStep);
        return cloneContext().withExecutionContext(setOptionCtx).withoutLock();
    }

    private Map<String, String> getOptionValues(GlusterVolumeEntity volume, GlusterVolumeOptionEntity option) {
        Map<String, String> values = new HashMap<>();
        values.put(GlusterConstants.CLUSTER, getClusterName());
        values.put(GlusterConstants.VOLUME, volume.getName());
        values.put(GlusterConstants.OPTION_KEY, option.getKey());
        values.put(GlusterConstants.OPTION_VALUE, option.getValue());
        return values;
    }

    /**
     * Validates the number of bricks against the replica count or stripe count based on volume type
     */
    private boolean validateBricks(GlusterVolumeEntity volume) {
        List<GlusterBrickEntity> bricks = volume.getBricks();
        if (bricks.isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
            return false;
        }

        int brickCount = bricks.size();
        int replicaCount = volume.getReplicaCount();
        int stripeCount = volume.getStripeCount();

        if (volume.getVolumeType().isReplicatedType() && replicaCount < 2) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_REPLICA_COUNT_MIN_2);
            return false;
        }
        if (volume.getVolumeType().isStripedType() && stripeCount < 4) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STRIPE_COUNT_MIN_4);
            return false;
        }

        switch (volume.getVolumeType()) {
        case REPLICATE:
            if (brickCount != replicaCount) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_REPLICATE);
                return false;
            }
            break;
        case DISTRIBUTED_REPLICATE:
            if (brickCount < replicaCount || Math.IEEEremainder(brickCount, replicaCount) != 0) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_DISTRIBUTED_REPLICATE);
                return false;
            }
            break;
        case STRIPE:
            if (brickCount != stripeCount) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_STRIPE);
                return false;
            }
            break;
        case DISTRIBUTED_STRIPE:
            if (brickCount <= stripeCount || Math.IEEEremainder(brickCount, stripeCount) != 0) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_DISTRIBUTED_STRIPE);
                return false;
            }
            break;
        case STRIPED_REPLICATE:
            if ( Math.IEEEremainder(brickCount, stripeCount * replicaCount) != 0) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_STRIPED_REPLICATE);
                return false;
            }
            break;
        case DISTRIBUTED_STRIPED_REPLICATE:
            if ( brickCount <= stripeCount * replicaCount || Math.IEEEremainder(brickCount, stripeCount * replicaCount) != 0) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_INVALID_BRICK_COUNT_FOR_DISTRIBUTED_STRIPED_REPLICATE);
                return false;
            }
            break;
        default:
            break;
        }

        boolean ret = updateBrickServerAndInterfaceNames(bricks, true) && validateDuplicateBricks(bricks);
        //only validate same server check for HC clusters.
        if (getCluster().supportsGlusterService() && getCluster().supportsVirtService()) {
            ret = ret && validateNotSameServer(bricks, replicaCount);
        }
        return ret;
    }

    private void setBrickOrder(List<GlusterBrickEntity> bricks) {
        for (int i = 0; i < bricks.size(); i++) {
            bricks.get(i).setBrickOrder(i);
        }
    }

    private void addVolumeToDb(final GlusterVolumeEntity createdVolume) {
        // volume fetched from VDSM doesn't contain cluster id as
        // GlusterFS is not aware of multiple clusters
        createdVolume.setClusterId(getClusterId());
        glusterVolumeDao.save(createdVolume);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (!getSucceeded()) {
            // Success need not be logged at the end of execution,
            // as it is already logged by executeCommand()
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_CREATE_FAILED : errorType;
        }
        return super.getAuditLogTypeValue();
    }
}
