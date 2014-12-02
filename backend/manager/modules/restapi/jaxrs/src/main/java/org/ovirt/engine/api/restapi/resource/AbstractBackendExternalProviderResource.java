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

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.ExternalProvider;
import org.ovirt.engine.api.resource.ExternalProviderCertificatesResource;
import org.ovirt.engine.api.resource.ExternalProviderResource;
import org.ovirt.engine.core.common.action.ImportProviderCertificateParameters;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.CertificateInfo;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendExternalProviderResource<R extends ExternalProvider>
        extends AbstractBackendActionableResource<R, Provider>
        implements ExternalProviderResource<R> {
    public AbstractBackendExternalProviderResource(String id, Class<R> modelType, String... subCollections) {
        super(id, modelType, Provider.class, subCollections);
    }

    @Override
    public R get() {
        return performGet(VdcQueryType.GetProviderById, new IdQueryParameters(guid));
    }

    @Override
    public R update(R incoming) {
        return performUpdate(
            incoming,
            new QueryIdResolver<Guid>(VdcQueryType.GetProviderById, IdQueryParameters.class),
            VdcActionType.UpdateProvider,
            new UpdateParametersProvider()
        );
    }

    protected class UpdateParametersProvider implements ParametersProvider<R, Provider> {
        @Override
        public VdcActionParametersBase getParameters(R incoming, Provider entity) {
            return new ProviderParameters(map(incoming, entity));
        }
    }

    @Override
    protected R doPopulate(R model, Provider entity) {
        return model;
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
        List<CertificateInfo> entities = getBackendCollection(
            CertificateInfo.class,
                VdcQueryType.GetProviderCertificateChain, new ProviderQueryParameters(provider)
        );
        if (entities.size() == 0) {
            return null;
        }
        return performAction(VdcActionType.ImportProviderCertificate,
                new ImportProviderCertificateParameters(provider, entities.get(0).getPayload()));
    }

    @Override
    public ExternalProviderCertificatesResource getCertificates() {
        return inject(new BackendExternalProviderCertificatesResource(id));
    }
}
