package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.AddClusterNetworkClusterValidator;
import org.ovirt.engine.core.bll.network.cluster.DefaultManagementNetworkFinder;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterValidatorBase;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.ClusterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class AddClusterCommand<T extends ManagementNetworkOnClusterOperationParameters>
        extends ClusterOperationCommandBase<T> {

    public static final String DefaultNetworkDescription = "Default Management Network";

    @Inject
    private DefaultManagementNetworkFinder defaultManagementNetworkFinder;

    @Inject
    protected ClusterDao clusterDao;

    @Inject
    protected NetworkClusterDao networkClusterDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private ClusterFeatureDao clusterFeatureDao;

    @Inject
    private InterfaceDao interfaceDao;

    private Network managementNetwork;

    public AddClusterCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStoragePoolId(getCluster().getStoragePoolId());
    }

    @Override
    protected void init() {
        updateMigrateOnError();
    }

    @Override
    protected void executeCommand() {
        Cluster cluster = getCluster();
        cluster.setArchitecture(getArchitecture());
        setDefaultSwitchTypeIfNeeded();

        checkMaxMemoryOverCommitValue();
        cluster.setDetectEmulatedMachine(true);
        getClusterDao().save(cluster);

        alertIfFencingDisabled();

        // add default network
        if (getParameters().getCluster().getStoragePoolId() != null) {
            attachManagementNetwork();
        }

        // create default CPU profile for supported clusters.
        addDefaultCpuProfile();

        if (CollectionUtils.isNotEmpty(cluster.getAddtionalFeaturesSupported())) {
            for (SupportedAdditionalClusterFeature feature : cluster.getAddtionalFeaturesSupported()) {
                feature.setClusterId(cluster.getId());
            }
            clusterFeatureDao.addAllSupportedClusterFeature(cluster.getAddtionalFeaturesSupported());
        }

        setActionReturnValue(cluster.getId());
        setSucceeded(true);
    }

    private void addDefaultCpuProfile() {
        CpuProfile cpuProfile = CpuProfileHelper.createCpuProfile(getParameters().getCluster().getId(),
                getParameters().getCluster().getName());

        CpuProfileParameters cpuProfileAddParameters = new CpuProfileParameters(cpuProfile);
        cpuProfileAddParameters.setAddPermissions(true);
        cpuProfileAddParameters.setParametersCurrentUser(getCurrentUser());
        cpuProfileAddParameters.setSessionId(getContext().getEngineContext().getSessionId());

        VdcReturnValueBase addCpuProfileReturnValue = getBackend().runAction(VdcActionType.AddCpuProfile, cpuProfileAddParameters);
        cpuProfile.setId(addCpuProfileReturnValue.getActionReturnValue());

    }

    private void attachManagementNetwork() {
        final NetworkCluster networkCluster = createManagementNetworkCluster();
        networkCluster.setClusterId(getClusterId());
        networkClusterDao.save(networkCluster);
    }

    private Guid getManagementNetworkId() {
        return getParameters().getManagementNetworkId();
    }

    private Network getManagementNetworkById() {
        final Guid managementNetworkId = getManagementNetworkId();
        return networkDao.get(managementNetworkId);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_CLUSTER
                : AuditLogType.USER_ADD_CLUSTER_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
    }

    @Override
    protected boolean validate() {
        final ClusterValidator validator = new ClusterValidator(
                getDbFacade(), getCluster(), getCpuFlagsManagerHandler());

        return validate(validator.nameNotUsed())
                && validate(validator.cpuTypeSupportsVirtService())
                && validate(validator.versionSupported())
                && validate(validator.dataCenterVersionMismatch())
                && validate(validator.dataCenterExists())
                && validate(validator.localStoragePoolAttachedToSingleCluster())
                && validate(validator.clusterServiceDefined())
                && validate(validator.mixedClusterServicesSupported())
                && validate(validator.attestationServerConfigured())
                && validate(validator.migrationSupported(getArchitecture()))
                && validateClusterPolicy(null)
                && validateManagementNetwork();
    }

    private boolean validateManagementNetwork() {
        if (getManagementNetworkId() == null) {
            return findDefaultManagementNetwork();
        } else {
            return validateInputManagementNetwork();
        }
    }

    private boolean findDefaultManagementNetwork() {
        managementNetwork =
                defaultManagementNetworkFinder.findDefaultManagementNetwork(getCluster().getStoragePoolId());
        if (getManagementNetwork() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_DEFAULT_MANAGEMENT_NETWORK_NOT_FOUND);
            return false;
        }
        return true;
    }

    private boolean validateInputManagementNetwork() {
        setManagementNetwork(getManagementNetworkById());

        if (getManagementNetwork() == null) {
            addValidationMessage(EngineMessage.NETWORK_NOT_EXISTS);
            return false;
        }

        final NetworkClusterValidatorBase networkClusterValidator = createNetworkClusterValidator();
        return validate(networkClusterValidator.networkBelongsToClusterDataCenter(getCluster(), getManagementNetwork()))
                && validate(networkClusterValidator.managementNetworkRequired(getManagementNetwork()))
                && validate(networkClusterValidator.managementNetworkNotExternal(getManagementNetwork()));
    }

    private AddClusterNetworkClusterValidator createNetworkClusterValidator() {
        final NetworkCluster networkCluster = createManagementNetworkCluster();
        return new AddClusterNetworkClusterValidator(
                interfaceDao,
                networkDao,
                networkCluster);
    }

    private NetworkCluster createManagementNetworkCluster() {
        return new NetworkCluster(
                getClusterId(),
                getManagementNetwork().getId(),
                NetworkStatus.OPERATIONAL,
                true,
                true,
                true,
                true,
                false);
    }

    protected Network getManagementNetwork() {
        return managementNetwork;
    }

    protected void setManagementNetwork(Network managementNetwork) {
        this.managementNetwork = managementNetwork;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getCluster().getStoragePoolId(),
                VdcObjectType.StoragePool,
                getActionType().getActionGroup()));
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

}
