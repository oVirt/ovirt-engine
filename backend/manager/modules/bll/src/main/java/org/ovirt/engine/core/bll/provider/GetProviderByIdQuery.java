/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.bll.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetProviderByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderDao dao;

    public GetProviderByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid id = getParameters().getId();
        Provider<?> provider = dao.get(id);
        getQueryReturnValue().setReturnValue(provider);
    }
}
