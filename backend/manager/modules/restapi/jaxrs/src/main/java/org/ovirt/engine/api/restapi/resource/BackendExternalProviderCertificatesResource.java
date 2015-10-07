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
import org.ovirt.engine.api.model.Certificates;
import org.ovirt.engine.api.resource.ExternalProviderCertificateResource;
import org.ovirt.engine.api.resource.ExternalProviderCertificatesResource;
import org.ovirt.engine.core.common.businessentities.CertificateInfo;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendExternalProviderCertificatesResource
        extends AbstractBackendCollectionResource<Certificate, CertificateInfo>
        implements ExternalProviderCertificatesResource {
    /**
     * The identifier of the provider.
     */
    private String providerId;

    public BackendExternalProviderCertificatesResource(String providerId) {
        super(Certificate.class, CertificateInfo.class);
        this.providerId = providerId;
    }

    @Override
    public Certificates list() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, providerId);
        ProviderQueryParameters parameters = new ProviderQueryParameters();
        parameters.setProvider(provider);
        return mapCollection(getBackendCollection(VdcQueryType.GetProviderCertificateChain, parameters));
    }

    protected Certificates mapCollection(List<CertificateInfo> entities) {
        Certificates collection = new Certificates();
        if (entities != null) {
            for (int i = 0; i < entities.size(); i++) {
                CertificateInfo entity = entities.get(i);
                Certificate model = populate(map(entity), entity);
                model.setId(String.valueOf(i));
                collection.getCertificates().add(model);
            }
        }
        return collection;
    }

    @Override
    public ExternalProviderCertificateResource getCertificateResource(String id) {
        return inject(new BackendExternalProviderCertificateResource(id, providerId));
    }
}
