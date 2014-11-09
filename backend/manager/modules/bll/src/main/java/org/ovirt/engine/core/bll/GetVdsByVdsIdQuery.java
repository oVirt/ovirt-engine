package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVdsByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VDS vds = getDbFacade()
                .getVdsDao()
                .get(getParameters().getId());

        if (vds != null) {
            vds.setCpuName(CpuFlagsManagerHandler.findMaxServerCpuByFlags(vds.getCpuFlags(),
                    vds.getVdsGroupCompatibilityVersion()));
        }

        getQueryReturnValue().setReturnValue(vds);
    }
}
