package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;

public class GetVdsByVdsIdQuery<P extends GetVdsByVdsIdParameters> extends QueriesCommandBase<P> {
    public GetVdsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VDS vds = getDbFacade()
                .getVdsDao()
                .get(getParameters().getVdsId());

        if (vds != null) {
            vds.setCpuName(CpuFlagsManagerHandler.FindMaxServerCpuByFlags(vds.getCpuFlags(),
                    vds.getVdsGroupCompatibilityVersion()));
        }

        getQueryReturnValue().setReturnValue(vds);
    }
}
