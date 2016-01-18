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
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class AddVdsGroupCommand<T extends ManagementNetworkOnClusterOperationParameters>
        extends VdsGroupOperationCommandBase<T> {

    public static final String DefaultNetworkDescription = "Default Management Network";

    @Inject
    private DefaultManagementNetworkFinder defaultManagementNetworkFinder;

    @Inject
    protected VdsGroupDao vdsGroupDao;

    @Inject
    protected NetworkClusterDao networkClusterDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private ClusterFeatureDao clusterFeatureDao;

    @Inject
    private InterfaceDao interfaceDao;

    private Network managementNetwork;

    public AddVdsGroupCommand(T parameters) {
        super(parameters);
        setStoragePoolId(getVdsGroup().getStoragePoolId());
        updateMigrateOnError();
    }

    public AddVdsGroupCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStoragePoolId(getVdsGroup().getStoragePoolId());
        updateMigrateOnError();
    }

    @Override
    protected void init() {
        updateMigrateOnError();
    }

    @Override
    protected void executeCommand() {
        VDSGroup vdsGroup = getVdsGroup();
        vdsGroup.setArchitecture(getArchitecture());

        checkMaxMemoryOverCommitValue();
        vdsGroup.setDetectEmulatedMachine(true);
        getVdsGroupDao().save(vdsGroup);

        alertIfFencingDisabled();

        // add default network
        if (getParameters().getVdsGroup().getStoragePoolId() != null) {
            attachManagementNetwork();
        }

        // create default CPU profile for supported clusters.
        addDefaultCpuProfile();

        if (CollectionUtils.isNotEmpty(vdsGroup.getAddtionalFeaturesSupported())) {
            for (SupportedAdditionalClusterFeature feature : vdsGroup.getAddtionalFeaturesSupported()) {
                feature.setClusterId(vdsGroup.getId());
            }
            clusterFeatureDao.addAllSupportedClusterFeature(vdsGroup.getAddtionalFeaturesSupported());
        }

        setActionReturnValue(vdsGroup.getId());
        setSucceeded(true);
    }

    private void addDefaultCpuProfile() {
        CpuProfile cpuProfile = CpuProfileHelper.createCpuProfile(getParameters().getVdsGroup().getId(),
                getParameters().getVdsGroup().getName());

        CpuProfileParameters cpuProfileAddParameters = new CpuProfileParameters(cpuProfile);
        cpuProfileAddParameters.setAddPermissions(true);
        cpuProfileAddParameters.setParametersCurrentUser(getCurrentUser());
        cpuProfileAddParameters.setSessionId(getContext().getEngineContext().getSessionId());

        getBackend().runAction(VdcActionType.AddCpuProfile, cpuProfileAddParameters);
    }

    private void attachManagementNetwork() {
        final NetworkCluster networkVdsGroup = createManagementNetworkCluster();
        networkVdsGroup.setClusterId(getVdsGroupId());
        networkClusterDao.save(networkVdsGroup);
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
        return getSucceeded() ? AuditLogType.USER_ADD_VDS_GROUP
                : AuditLogType.USER_ADD_VDS_GROUP_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(EngineMessage.VAR__ACTION__CREATE);
    }

    @Override
    protected boolean canDoAction() {
        final ClusterValidator validator = new ClusterValidator(getDbFacade(), getVdsGroup());

        return validate(validator.nameNotUsed())
                && validate(validator.cpuTypeSupportsVirtService())
                && validate(validator.versionSupported())
                && validate(validator.dataCenterVersionMismatch())
                && validate(validator.dataCenterExists())
                && validate(validator.localStoragePoolAttachedToSingleCluster())
                && validate(validator.qosBaloonSupported())
                && validate(validator.glusterServiceSupported())
                && validate(validator.clusterServiceDefined())
                && validate(validator.mixedClusterServicesSupported())
                && validate(validator.attestationServerConfigured())
                && validate(validator.migrationSupported(getArchitecture()))
                && validateClusterPolicy(null)
                && validate(validator.virtIoRngSupported())
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
        setManagementNetwork(defaultManagementNetworkFinder.findDefaultManagementNetwork(getVdsGroup().getStoragePoolId()));

        if (getManagementNetwork() == null) {
            addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_DEFAULT_MANAGEMENT_NETWORK_NOT_FOUND);
            return false;
        }
        return true;
    }

    private boolean validateInputManagementNetwork() {
        setManagementNetwork(getManagementNetworkById());

        if (getManagementNetwork() == null) {
            addCanDoActionMessage(EngineMessage.NETWORK_NOT_EXISTS);
            return false;
        }

        final NetworkClusterValidatorBase networkClusterValidator = createNetworkClusterValidator();
        return validate(networkClusterValidator.networkBelongsToClusterDataCenter(getVdsGroup(), getManagementNetwork()))
                && validate(networkClusterValidator.managementNetworkRequired(getManagementNetwork()))
                && validate(networkClusterValidator.managementNetworkNotExternal(getManagementNetwork()));
    }

    private AddClusterNetworkClusterValidator createNetworkClusterValidator() {
        final NetworkCluster networkCluster = createManagementNetworkCluster();
        return new AddClusterNetworkClusterValidator(
                interfaceDao,
                networkDao,
                networkCluster,
                getVdsGroup().getCompatibilityVersion());
    }

    private NetworkCluster createManagementNetworkCluster() {
        return new NetworkCluster(
                getVdsGroupId(),
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
        return Collections.singletonList(new PermissionSubject(getVdsGroup().getStoragePoolId(),
                VdcObjectType.StoragePool,
                getActionType().getActionGroup()));
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

}
