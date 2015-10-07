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

import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.Certificates;
import org.ovirt.engine.api.model.ExternalProvider;
import org.ovirt.engine.api.resource.ExternalProviderCertificatesResource;
import org.ovirt.engine.api.resource.ExternalProviderResource;
import org.ovirt.engine.core.common.action.ImportProviderCertificateParameters;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;

public abstract class AbstractBackendExternalProviderResource<R extends ExternalProvider>
        extends AbstractBackendActionableResource<R, Provider>
        implements ExternalProviderResource {
    public AbstractBackendExternalProviderResource(String id, Class<R> modelType, String... subCollections) {
        super(id, modelType, Provider.class, subCollections);
    }

    @Override
    public Response testConnectivity(Action action) {
        Provider provider = BackendExternalProviderHelper.getProvider(this, id);
        ProviderParameters parameters = new ProviderParameters(provider);
        return performAction(VdcActionType.TestProviderConnectivity, parameters);
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
            VdcActionType.ImportProviderCertificate,
            new ImportProviderCertificateParameters(provider, content)
        );
    }

    @Override
    public ExternalProviderCertificatesResource getCertificatesResource() {
        return inject(new BackendExternalProviderCertificatesResource(id));
    }
}
