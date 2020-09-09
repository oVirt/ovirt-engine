package org.ovirt.engine.core.bll.network.dc;

import static org.ovirt.engine.core.common.AuditLogType.NETWORK_ADD_NETWORK;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_ADD_NETWORK_FAILED;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_ADD_NETWORK_STARTED;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_ADD_NETWORK_START_ERROR;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_ADD_NOTHING_TO_DO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NetworkLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.HasStoragePoolValidator;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AddVnicProfileParameters;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AddNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkModification<T> {
    @Inject
    NetworkFilterDao networkFilterDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VnicProfileDao vnicProfileDao;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private NetworkLocking networkLocking;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public AddNetworkCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected void executeCommand() {
        getNetwork().setId(Guid.newGuid());
        NetworkUtils.setNetworkVdsmName(getNetwork());

        TransactionSupport.executeInNewTransaction(() -> {
            networkDao.save(getNetwork());

            networkHelper.addPermissionsOnNetwork(getUserId(), getNetwork().getId());
            return null;
        });

        // Run cluster attachment, AddVnicProfile and  auto-define in separated thread
        if (getParameters().isAsync()) {
            ExecutionHandler.setAsyncJob(getExecutionContext(), true);
            CompletableFuture.runAsync(this::runClusterAttachment, ThreadPoolUtil.getExecutorService())
                    .thenRunAsync(this::runAddVnicProfile).thenRunAsync(this::runAutodefine);
        } else {
            runClusterAttachment();
            runAddVnicProfile();
            runAutodefine();
        }

        getReturnValue().setActionReturnValue(getNetwork().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
    }

    @Override
    protected boolean validate() {
        HasStoragePoolValidator hasStoragePoolValidator = new HasStoragePoolValidator(getNetwork());
        AddNetworkValidator validator = getNetworkValidator();
        return validate(hasStoragePoolValidator.storagePoolExists())
                && validate(validator.stpForVmNetworkOnly())
                && validate(validator.portIsolationForVmNetworkOnly())
                && validate(validator.portIsolationNoExternalNetwork())
                && validate(validator.networkPrefixValid())
                && validate(validator.networkNameNotUsed())
                && validate(validator.networkNameNotUsedAsVdsmName())
                && validate(validator.qosExistsInDc())
                && (!getNetwork().isExternal() || externalNetworkValid(validator));
    }

    protected AddNetworkValidator getNetworkValidator() {
        return new AddNetworkValidator(getNetwork());
    }

    private boolean externalNetworkValid(AddNetworkValidator validator) {
        ProviderValidator providerValidator =
                new ProviderValidator(providerDao.get(getNetwork().getProvidedBy().getProviderId()));
        return validate(providerValidator.providerIsSet())
                && validate(validator.externalNetworkNewInDataCenter())
                && validate(validator.externalNetworkIsVmNetwork())
                && validate(validator.externalNetworkVlanValid())
                && validate(validator.providerPhysicalNetworkValid());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (!getSucceeded()) {
                return NETWORK_ADD_NETWORK_START_ERROR;
            } else if (skipHostSetupNetworks()) {
                return NETWORK_ADD_NOTHING_TO_DO;
            } else {
                return NETWORK_ADD_NETWORK_STARTED;
            }
        case END_SUCCESS:
            return NETWORK_ADD_NETWORK;
        }
        return NETWORK_ADD_NETWORK_FAILED;
    }

    @Override
    public ActionReturnValue endAction() {
        getExecutionContext().setShouldEndJob(true);
        return super.endAction();
    }

    private boolean skipHostSetupNetworks() {
        return getParameters().getNetworkClusterList() == null;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }
        StoragePool pool = storagePoolDao.get(getNetwork().getDataCenterId());
        jobProperties.put(VdcObjectType.Network.name().toLowerCase(), getNetwork().getName());
        jobProperties.put(VdcObjectType.StoragePool.name().toLowerCase(), pool.getName());
        return jobProperties;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        final Network network = getNetwork();
        Map<String, Pair<String, String>> locks = new HashMap<>();

        if (getNetworkName() != null) {
            locks.put(getNetworkName(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.NETWORK,
                            EngineMessage.ACTION_TYPE_FAILED_NETWORK_IS_USED));
        }


        if (network.isExternal() && !isInternalExecution()) {
            locks.putAll(networkLocking.getNetworkProviderLock(network.getProvidedBy().getProviderId()));
        }

        return locks;
    }

    private void runClusterAttachment() {
        List<NetworkCluster> networkAttachments = getParameters().getNetworkClusterList();
        if (networkAttachments != null) {
            attachToClusters(networkAttachments, getNetwork().getId());
        }
    }

    private void attachToClusters(List<NetworkCluster> networkAttachments, Guid networkId) {
        networkAttachments.forEach(networkCluster -> networkCluster.setNetworkId(networkId));
        ManageNetworkClustersParameters parameters = new ManageNetworkClustersParameters(networkAttachments);
        withRootCommandInfo(parameters);
        runInternalAction(ActionType.ManageNetworkClusters,
                parameters,
                getContext().clone().withoutLock());
    }

    private void runAddVnicProfile() {
        if (getNetwork().isVmNetwork() && getParameters().isVnicProfileRequired()) {
            VnicProfile vnicProfile = networkHelper.createVnicProfile(getNetwork());

            AddVnicProfileParameters vnicProfileParameters = new AddVnicProfileParameters(vnicProfile);
            vnicProfileParameters.setPublicUse(getParameters().isVnicProfilePublicUse());
            runInternalAction(ActionType.AddVnicProfile, vnicProfileParameters, getContext().clone().withoutLock());
        }
    }

    private void runAutodefine() {
        if (getNetwork().isVmNetwork() && !getNetwork().isExternal()) {
            runInternalAction(ActionType.AutodefineExternalNetwork,
                    new IdParameters(getNetwork().getId()),
                    getContext().clone().withoutLock());
        }
    }

    protected static class AddNetworkValidator extends NetworkValidator {

        public AddNetworkValidator(Network network) {
            super(network);
        }

        public ValidationResult externalNetworkVlanValid() {
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_WITH_VLAN_MUST_BE_CUSTOM)
                    .when(network.getProvidedBy().hasExternalVlanId() && !network.getProvidedBy()
                            .hasCustomPhysicalNetworkName());
        }

        /**
         * @return An error iff the network represents an external network that already exists in the data center that
         *         the network should be on.
         */
        public ValidationResult externalNetworkNewInDataCenter() {
            for (Network otherNetwork : getNetworks()) {
                if (network.getProvidedBy().equals(otherNetwork.getProvidedBy())) {
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_ALREADY_EXISTS);
                }
            }

            return ValidationResult.VALID;
        }

        /**
         * @return An error iff the network represents an external network is not a VM network, since we don't know how
         *         to handle non-VM external networks.
         */
        public ValidationResult externalNetworkIsVmNetwork() {
            return network.isVmNetwork() ? ValidationResult.VALID
                    : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_MUST_BE_VM_NETWORK);
        }

        /**
         * @return An error if the network selected as provider physical network does not exist in the data center or
         *         if label or vlan id is specified.
         */
        public ValidationResult providerPhysicalNetworkValid() {
            if (!network.getProvidedBy().isSetPhysicalNetworkId()) {
                return ValidationResult.VALID;
            }

            List<EngineMessage> errorMessages = new ArrayList<>();

            if (isLabelOrVlanSet()) {
                errorMessages.add(EngineMessage.ACTION_TYPE_FAILED_LABEL_AND_VLAN_CANNOT_BE_SET_WITH_PROVIDER_PHYSICAL_NETWORK);
            }
            if (!providerPhysicalNetworkInDataCenter()) {
                errorMessages.add(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_PHYSICAL_NETWORK_DOES_NOT_EXIST_ON_DC);
            }

            if (errorMessages.isEmpty()) {
                return ValidationResult.VALID;
            }
            return new ValidationResult(errorMessages);
        }

        private boolean isLabelOrVlanSet() {
            return network.getLabel() != null || network.getVlanId() != null;
        }

        private boolean providerPhysicalNetworkInDataCenter() {
            return getNetworks().stream().anyMatch(
                    otherNetwork -> network.getProvidedBy().getPhysicalNetworkId().equals(otherNetwork.getId()));
        }
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
