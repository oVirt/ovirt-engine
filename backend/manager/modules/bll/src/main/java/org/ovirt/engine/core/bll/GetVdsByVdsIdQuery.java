package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

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

    private static LogCompat log = LogFactoryCompat.getLog(GetVdsByVdsIdQuery.class);
}
