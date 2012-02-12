package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class GetVdsByVdsIdQuery<P extends GetVdsByVdsIdParameters> extends QueriesCommandBase<P> {
    public GetVdsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VDS vds = DbFacade.getInstance().getVdsDAO().get(getParameters().getVdsId());

        if (vds != null) {
            vds.setCpuName(CpuFlagsManagerHandler.FindMaxServerCpuByFlags(vds.getcpu_flags(),
                    vds.getvds_group_compatibility_version()));
        }

        getQueryReturnValue().setReturnValue(vds);
    }

    private static Log log = LogFactory.getLog(GetVdsByVdsIdQuery.class);
}
