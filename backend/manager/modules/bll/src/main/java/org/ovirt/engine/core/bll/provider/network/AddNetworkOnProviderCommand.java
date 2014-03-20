package org.ovirt.engine.core.bll.provider.network;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.dc.AddNetworkCommand;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AddNetworkOnProviderCommand<T extends AddNetworkStoragePoolParameters> extends AddNetworkCommand<T> {

    private Provider<?> provider;

    public AddNetworkOnProviderCommand(T parameters) {
        super(parameters);
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
    protected boolean canDoAction() {
        ProviderValidator validator = new ProviderValidator(getProvider());

        return validate(validator.providerIsSet()) && super.canDoAction();
    }

    @Override
    protected void executeCommand() {
        NetworkProviderProxy proxy = ProviderProxyFactory.getInstance().create(getProvider());
        getNetwork().getProvidedBy().setExternalId(proxy.add(getNetwork()));
        getNetwork().setVlanId(null);
        getNetwork().setLabel(null);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                addNetwork();
                return null;
            }
        });
    }

    private void addNetwork() {
        super.executeCommand();
    }

    @Override
    protected AddNetworkValidator getNetworkValidator() {
        return new AddNetworkOnProviderValidator(getNetwork());
    }

    protected class AddNetworkOnProviderValidator extends AddNetworkValidator {
        public AddNetworkOnProviderValidator(Network network) {
            super(network);
        }

        /**
         * External networks can't have an MTU set since the provider can't assure this.
         */
        @Override
        public ValidationResult mtuValid() {
            return network.getMtu() == 0 ? ValidationResult.VALID
                    : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_HAVE_MTU);
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
