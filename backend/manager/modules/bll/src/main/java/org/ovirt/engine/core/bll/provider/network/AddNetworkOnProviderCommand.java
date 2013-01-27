package org.ovirt.engine.core.bll.provider.network;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.network.dc.AddNetworkCommand;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class AddNetworkOnProviderCommand<T extends AddNetworkStoragePoolParameters> extends AddNetworkCommand<T> {

    private Provider<?> provider;

    public AddNetworkOnProviderCommand(T parameters) {
        super(parameters);
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
}
