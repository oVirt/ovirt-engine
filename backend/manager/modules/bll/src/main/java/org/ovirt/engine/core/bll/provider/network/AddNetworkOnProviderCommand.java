package org.ovirt.engine.core.bll.provider.network;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.dc.AddNetworkCommand;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AddNetworkOnProviderCommand<T extends AddNetworkStoragePoolParameters> extends AddNetworkCommand<T> {

    @Inject
    private ProviderDao providerDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    private Provider<?> provider;

    public AddNetworkOnProviderCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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

        TransactionSupport.executeInNewTransaction(() -> {
            super.executeCommand();
            getReturnValue().setActionReturnValue(getNetwork().getId());
            return null;
        });
        postAddNetwork(getProvider().getId(), getNetwork().getProvidedBy().getExternalId());
    }

    private void loadPhysicalNetworkProviderParameters() {
        Network physicalProviderNetwork = networkDao.get(getNetwork().getProvidedBy().getPhysicalNetworkId());
        getNetwork().getProvidedBy().setExternalVlanId(physicalProviderNetwork.getVlanId());
        getNetwork().getProvidedBy().setCustomPhysicalNetworkName(physicalProviderNetwork.getVdsmName());
    }

    protected void postAddNetwork(Guid providerId, String externalId) {
    }
}
