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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AddNetworkOnProviderCommand<T extends AddNetworkStoragePoolParameters> extends AddNetworkCommand<T> {

    @Inject
    private ProviderDao providerDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;

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
            provider = providerDao.get(getNetwork().getProvidedBy().getProviderId());
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
        if (getNetwork().getProvidedBy().isSetPhysicalNetworkId()) {
            loadPhysicalNetworkProviderParameters();
        }

        NetworkProviderProxy proxy = providerProxyFactory.create(getProvider());
        getNetwork().getProvidedBy().setExternalId(proxy.add(getNetwork()));
        getNetwork().setVlanId(null);
        getNetwork().setLabel(null);

        TransactionSupport.executeInNewTransaction(() -> {
            super.executeCommand();
            getReturnValue().setActionReturnValue(getNetwork().getId());
            return null;
        });
        postAddNetwork(getProvider().getId(), getNetwork().getProvidedBy().getExternalId());
    }

    private void loadPhysicalNetworkProviderParameters() {
        Network physicalProviderNetwork = networkDao.get(getNetwork().getProvidedBy().getPhysicalNetworkId());
        getNetwork().setVlanId(physicalProviderNetwork.getVlanId());
        getNetwork().setLabel(physicalProviderNetwork.getVdsmName());
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
    }

    protected void postAddNetwork(Guid providerId, String externalId) {
    }
}
