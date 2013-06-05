package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    public AddNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getNetwork().setId(Guid.newGuid());
        getNetworkDAO().save(getNetwork());
        NetworkHelper.addPermissions(getCurrentUser().getUserId(), getNetwork().getId(), getParameters().isPublicUse());
        getReturnValue().setActionReturnValue(getNetwork().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }

    @Override
    protected boolean canDoAction() {
        AddNetworkValidator validator = getNetworkValidator();
        return validate(validator.dataCenterExists())
                && validate(validator.vmNetworkSetCorrectly())
                && validate(validator.stpForVmNetworkOnly())
                && validate(validator.mtuValid())
                && validate(validator.networkPrefixValid())
                && validate(validator.networkNameNotUsed())
                && validate(validator.vlanIdNotUsed())
                && (getNetwork().getProvidedBy() == null || externalNetworkValid(validator));
    }

    protected AddNetworkValidator getNetworkValidator() {
        return new AddNetworkValidator(getNetwork());
    }

    private boolean externalNetworkValid(AddNetworkValidator validator) {
        ProviderValidator providerValidator =
                new ProviderValidator(getDbFacade().getProviderDao().get(getNetwork().getProvidedBy().getProviderId()));
        return validate(providerValidator.providerIsSet())
                && validate(validator.externalNetworkNewInDataCenter())
                && validate(validator.externalNetworkIsVmNetwork());
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

    protected static class AddNetworkValidator extends NetworkValidator {

        public AddNetworkValidator(Network network) {
            super(network);
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
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_ALREADY_EXISTS);
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
                    : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_MUST_BE_VM_NETWORK);
        }
    }
}
