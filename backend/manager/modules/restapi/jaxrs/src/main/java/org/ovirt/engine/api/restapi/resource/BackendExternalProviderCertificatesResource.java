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

import javax.ws.rs.core.Response;
import java.util.List;

import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.Certificates;
import org.ovirt.engine.api.resource.ExternalProviderCertificateResource;
import org.ovirt.engine.api.resource.ExternalProviderCertificatesResource;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendExternalProviderCertificatesResource
        extends AbstractBackendCollectionResource<Certificate, java.security.cert.Certificate>
        implements ExternalProviderCertificatesResource {
    /**
     * The identifier of the provider.
     */
    private String providerId;

    public BackendExternalProviderCertificatesResource(String providerId) {
        super(Certificate.class, java.security.cert.Certificate.class);
        this.providerId = providerId;
    }

    @Override
    public Certificates list() {
        Provider provider = BackendExternalProviderHelper.getProvider(this, providerId);
        ProviderQueryParameters parameters = new ProviderQueryParameters();
        parameters.setProvider(provider);
        return mapCollection(getBackendCollection(VdcQueryType.GetProviderCertificateChain, parameters));
    }

    @Override
    protected Certificate doPopulate(Certificate model, java.security.cert.Certificate entity) {
        return model;
    }

    protected Certificates mapCollection(List<java.security.cert.Certificate> entities) {
        Certificates collection = new Certificates();
        if (entities != null) {
            for (int i = 0; i < entities.size(); i++) {
                java.security.cert.Certificate entity = entities.get(i);
                Certificate model = populate(map(entity), entity);
                model.setId(String.valueOf(i));
                collection.getCertificates().add(model);
            }
        }
        return collection;
    }

    @Override
    protected Response performRemove(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SingleEntityResource
    public ExternalProviderCertificateResource getCertificate(String id) {
        return inject(new BackendExternalProviderCertificateResource(id, providerId));
    }
}
