/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.Certificates;
import org.ovirt.engine.api.model.ExternalProvider;
import org.ovirt.engine.api.resource.ExternalProviderCertificatesResource;
import org.ovirt.engine.api.resource.ExternalProviderResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportProviderCertificateParameters;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.Provider;

public abstract class AbstractBackendExternalProviderResource<R extends ExternalProvider>
        extends AbstractBackendActionableResource<R, Provider>
        implements ExternalProviderResource {
    public AbstractBackendExternalProviderResource(String id, Class<R> modelType) {
        super(id, modelType, Provider.class);
    }

    @Override
    public Response testConnectivity(Action action) {
        Provider provider = BackendExternalProviderHelper.getProvider(this, id);
        ProviderParameters parameters = new ProviderParameters(provider);
        return performAction(ActionType.TestProviderConnectivity, parameters);
    }

    @Override
    public Response importCertificates(Action action) {
        Provider provider = BackendExternalProviderHelper.getProvider(this, id);
        validateParameters(action, "certificates.content");
        String content = null;
        Certificates certificates = action.getCertificates();
        if (certificates != null) {
            List<Certificate> list = certificates.getCertificates();
            if (!CollectionUtils.isEmpty(list)) {
                content = list.get(0).getContent();
            }
        }
        return performAction(
            ActionType.ImportProviderCertificate,
            new ImportProviderCertificateParameters(provider, content)
        );
    }

    @Override
    public ExternalProviderCertificatesResource getCertificatesResource() {
        return inject(new BackendExternalProviderCertificatesResource(id));
    }
}
