package org.ovirt.engine.core.bll.provider.network;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.dc.AddNetworkCommand;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AddNetworkOnProviderCommand<T extends AddNetworkStoragePoolParameters> extends AddNetworkCommand<T> {

    @Inject
    private VmDao vmDao;

    private Provider<?> provider;

    public AddNetworkOnProviderCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    private Provider<?> getProvider() {
        if (provider == null) {
            provider = getDbFacade().getProviderDao().get(getNetwork().getProvidedBy().getProviderId());
        }

        return provider;
    }

    @Override
    protected boolean validate() {
        ProviderValidator validator = new ProviderValidator(getProvider());

        return validate(validator.providerIsSet()) && validate(validator.validateReadOnlyActions()) && super.validate();
    }

    @Override
    protected void executeCommand() {
        NetworkProviderProxy proxy = ProviderProxyFactory.getInstance().create(getProvider());
        getNetwork().getProvidedBy().setExternalId(proxy.add(getNetwork()));
        getNetwork().setVlanId(null);
        getNetwork().setLabel(null);

        TransactionSupport.executeInNewTransaction(() -> {
            super.executeCommand();
            getReturnValue().setActionReturnValue(getNetwork());
            return null;
        });
    }

    @Override
    protected AddNetworkValidator getNetworkValidator() {
        return new AddNetworkOnProviderValidator(vmDao, getNetwork());
    }

    protected class AddNetworkOnProviderValidator extends AddNetworkValidator {
        public AddNetworkOnProviderValidator(VmDao vmDao, Network network) {
            super(vmDao, network);
        }

        /**
         * External networks can't have an MTU set since the provider can't assure this.
         */
        @Override
        public ValidationResult mtuValid() {
            return network.getMtu() == 0 ? ValidationResult.VALID
                    : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_HAVE_MTU);
        }

        /**
         * VLAN ID is not relevant in this case, so don't check it.
         */
        @Override
        public ValidationResult vlanIdNotUsed() {
            return ValidationResult.VALID;
        }
    }
}
