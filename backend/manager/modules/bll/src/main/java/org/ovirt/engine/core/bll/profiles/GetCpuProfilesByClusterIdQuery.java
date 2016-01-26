package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetCpuProfilesByClusterIdQuery extends QueriesCommandBase<IdQueryParameters> {

    /**
     *
     * This query gets the cpu profiles.
     *
     * @param parameters
     *            The parameters for the query. This should specify the cluster id and if
     *            the list should be filtered according to user and action group.
     */
    public GetCpuProfilesByClusterIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getCpuProfileDao()
                .getAllForCluster(getParameters().getId(), getUserID(), getParameters().isFiltered(), ActionGroup.ASSIGN_CPU_PROFILE));
    }

}
