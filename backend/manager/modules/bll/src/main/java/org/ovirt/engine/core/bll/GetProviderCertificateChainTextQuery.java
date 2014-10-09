/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.core.bll;

import java.security.cert.Certificate;
import java.util.List;

import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;

public class GetProviderCertificateChainTextQuery<P extends ProviderQueryParameters> extends QueriesCommandBase<P> {
    public GetProviderCertificateChainTextQuery(P parameters) {
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
            for (Certificate certificate : chain) {
                certStringBuilder.append(certificate.toString());
                certStringBuilder.append('\n');
            }
        }
        return certStringBuilder.toString();
    }
}
