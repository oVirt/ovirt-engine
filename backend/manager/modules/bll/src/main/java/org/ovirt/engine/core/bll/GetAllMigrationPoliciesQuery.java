package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.migration.ConvergenceConfigProvider;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class GetAllMigrationPoliciesQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private ConvergenceConfigProvider provider;

    public GetAllMigrationPoliciesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(provider.getAllMigrationPolicies());
    }
}
