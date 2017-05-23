package org.ovirt.engine.core.bll.storage.disk.cinder;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public abstract class CinderQueryBase<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    private OpenStackVolumeProviderProxy volumeProviderProxy;

    public CinderQueryBase(P parameters, EngineContext context) {
        super(parameters, context);
    }

    public OpenStackVolumeProviderProxy getVolumeProviderProxy() {
        if (volumeProviderProxy == null) {
            volumeProviderProxy = OpenStackVolumeProviderProxy.getFromStorageDomainId(
                    getParameters().getId(), getUserID(), getParameters().isFiltered(), providerProxyFactory);
        }
        return volumeProviderProxy;
    }
}
