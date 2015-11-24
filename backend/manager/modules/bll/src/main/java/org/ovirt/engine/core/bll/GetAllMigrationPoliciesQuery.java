package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.migration.ConvergenceConfigProvider;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllMigrationPoliciesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private ConvergenceConfigProvider provider;

    public GetAllMigrationPoliciesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(provider.getAllMigrationPolicies());
    }
}
