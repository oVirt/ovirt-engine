package org.ovirt.engine.core.bll.profiles;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

public class GetCpuProfilesByClusterIdQuery extends QueriesCommandBase<IdQueryParameters> {
    @Inject
    private CpuProfileDao cpuProfileDao;

    /**
     *
     * This query gets the cpu profiles.
     *
     * @param parameters
     *            The parameters for the query. This should specify the cluster id and if
     *            the list should be filtered according to user and action group.
     * @param engineContext The context the query is run under
     */
    public GetCpuProfilesByClusterIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(cpuProfileDao
                .getAllForCluster(getParameters().getId(), getUserID(), getParameters().isFiltered(), ActionGroup.ASSIGN_CPU_PROFILE));
    }

}
