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

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.resource.ExternalProviderCertificateResource;
import org.ovirt.engine.core.common.businessentities.CertificateInfo;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExternalProviderCertificateResource
        extends AbstractBackendActionableResource<Certificate, CertificateInfo>
        implements ExternalProviderCertificateResource {

    private String providerId;

    protected BackendExternalProviderCertificateResource(String id, String providerId) {
        super(id, Certificate.class, CertificateInfo.class);
        this.providerId = providerId;
    }

    @Override
    public Certificate get() {
        // The resource identifier is actually the index of the certificate in the chain:
        int i;
        try {
            i = Integer.parseInt(id);
        }
        catch (NumberFormatException exception) {
            return notFound();
        }

        // The backend doesn't have a mechanism to retrieve just one of the certificates of the chain, so we have to
        // retrieve them all and find the one that matches the identifier:
        Provider provider = BackendExternalProviderHelper.getProvider(this, providerId);
        ProviderQueryParameters parameters = new ProviderQueryParameters();
        parameters.setProvider(provider);
        List<CertificateInfo> entities = getBackendCollection(
            CertificateInfo.class,
            VdcQueryType.GetProviderCertificateChain, parameters
        );
        if (entities != null && i >= 0 && i < entities.size()) {
            CertificateInfo entity = entities.get(i);
            Certificate model = populate(map(entity), entity);
            model.setId(id);
            return model;
        }

        // No luck:
        return notFound();
    }

    @Override
    protected Guid asGuidOr404(String id) {
        return null;
    }
}
