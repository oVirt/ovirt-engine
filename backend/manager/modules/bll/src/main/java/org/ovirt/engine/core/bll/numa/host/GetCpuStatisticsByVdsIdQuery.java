package org.ovirt.engine.core.bll.numa.host;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.CpuStatistics;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetCpuStatisticsByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetCpuStatisticsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
       List<CpuStatistics> stats = getDbFacade().getVdsCpuStatisticsDao()
               .getAllCpuStatisticsByVdsId(getParameters().getId());
        getQueryReturnValue().setReturnValue(stats);
    }

}
