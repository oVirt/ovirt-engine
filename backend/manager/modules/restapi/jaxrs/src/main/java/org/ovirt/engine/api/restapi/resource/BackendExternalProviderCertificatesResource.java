/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
import org.ovirt.engine.core.common.queries.QueryType;

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
        return mapCollection(getBackendCollection(QueryType.GetProviderCertificateChain, parameters));
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
