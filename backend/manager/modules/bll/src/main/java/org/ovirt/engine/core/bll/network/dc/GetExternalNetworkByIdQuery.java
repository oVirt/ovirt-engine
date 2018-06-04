package org.ovirt.engine.core.bll.network.dc;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetExternalNetworkByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkDao networkDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    @Inject
    private ProviderDao providerDao;

    private Network network;
    private Provider provider;

    private Network getNetwork() {
        if (network == null) {
            network = networkDao.get(getParameters().getId());
        }
        return network;
    }

    private Provider<?> getProvider() {
        if (provider == null) {
            provider = providerDao.get(getNetwork().getProvidedBy().getProviderId());
        }
        return provider;
    }

    public GetExternalNetworkByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        NetworkProviderProxy proxy = providerProxyFactory.create(getProvider());
        getQueryReturnValue().setReturnValue(proxy.get(getNetwork().getProvidedBy().getExternalId()));
    }

    @Override
    protected boolean validateInputs() {
        if (!super.validateInputs()) {
            return false;
        }

        if (getNetwork() == null) {
            getQueryReturnValue().setExceptionMessage(EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        if (!getNetwork().isExternal()) {
            getQueryReturnValue().setExceptionMessage(EngineMessage.NETWORK_IS_NOT_EXTERNAL.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        return true;
    }
}
