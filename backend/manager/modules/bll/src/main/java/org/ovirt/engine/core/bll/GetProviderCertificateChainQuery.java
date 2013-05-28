package org.ovirt.engine.core.bll;

import java.security.cert.Certificate;
import java.util.List;

import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;

public class GetProviderCertificateChainQuery<P extends ProviderQueryParameters> extends QueriesCommandBase<P> {
    public GetProviderCertificateChainQuery(P parameters) {
        super(parameters);
    }

    private Provider getProvider() {
        return getParameters().getProvider();
    }

    @Override
    protected void executeQueryCommand() {
        Provider provider = getProvider();
        ProviderProxy proxy = ProviderProxyFactory.getInstance().create(provider);
        getQueryReturnValue().setReturnValue(chainToString(proxy.getCertificateChain()));
    }

    private String chainToString(List<? extends Certificate> chain) {
        StringBuilder certStringBuilder = new StringBuilder();
        if (chain != null) {
            for( Certificate certificate : chain ) {
                certStringBuilder.append(certificate.toString());
                certStringBuilder.append('\n');
            }
        }
        return certStringBuilder.toString();
    }
}
