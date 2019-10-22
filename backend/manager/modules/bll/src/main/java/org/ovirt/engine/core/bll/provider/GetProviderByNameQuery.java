/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.bll.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetProviderByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderDao dao;

    public GetProviderByNameQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        String name = getParameters().getName();
        Provider<?> provider = dao.getByName(name);
        getQueryReturnValue().setReturnValue(provider);
    }
}
