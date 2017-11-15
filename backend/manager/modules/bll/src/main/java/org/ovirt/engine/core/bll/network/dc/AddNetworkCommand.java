package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.HasStoragePoolValidator;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.NetworkUtils;
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
    private VmDao vmDao;

    public AddNetworkCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeCommand() {
        getNetwork().setId(Guid.newGuid());
        NetworkUtils.setNetworkVdsmName(getNetwork());

        TransactionSupport.executeInNewTransaction(() -> {
            networkDao.save(getNetwork());

            if (getNetwork().isVmNetwork() && getParameters().isVnicProfileRequired()) {
                vnicProfileDao.save(networkHelper.createVnicProfile(getNetwork()));
            }

            networkHelper.addPermissionsOnNetwork(getCurrentUser().getId(), getNetwork().getId());
            return null;
        });

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
                && validate(validator.mtuValid())
                && validate(validator.networkPrefixValid())
                && validate(validator.networkNameNotUsed())
                && validate(validator.networkNameNotUsedAsVdsmName())
                && validate(validator.qosExistsInDc())
                && (!getNetwork().isExternal() || externalNetworkValid(validator));
    }

    protected AddNetworkValidator getNetworkValidator() {
        return new AddNetworkValidator(vmDao, getNetwork());
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
        return getSucceeded() ? AuditLogType.NETWORK_ADD_NETWORK : AuditLogType.NETWORK_ADD_NETWORK_FAILED;
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
        if (getNetworkName() == null) {
            return null;
        }

        Map<String, Pair<String, String>> locks = Collections.singletonMap(
                getNetworkName(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.NETWORK,
                        EngineMessage.ACTION_TYPE_FAILED_NETWORK_IS_USED));

        return locks;
    }

    protected static class AddNetworkValidator extends NetworkValidator {

        public AddNetworkValidator(VmDao vmDao, Network network) {
            super(vmDao, network);
        }

        public ValidationResult externalNetworkVlanValid() {
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_WITH_VLAN_MUST_BE_LABELED)
                    .when(network.getVlanId() != null && network.getLabel() == null);
        }

        @Override
        protected DbFacade getDbFacade() {
            return super.getDbFacade();
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
}
