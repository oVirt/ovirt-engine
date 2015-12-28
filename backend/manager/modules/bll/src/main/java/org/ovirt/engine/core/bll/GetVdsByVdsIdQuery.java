package org.ovirt.engine.core.bll;

import javax.inject.Inject;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVdsByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Override
    protected void executeQueryCommand() {
        VDS vds = getDbFacade()
                .getVdsDao()
                .get(getParameters().getId());

        if (vds != null) {
            vds.setCpuName(getCpuFlagsManagerHandler().findMaxServerCpuByFlags(vds.getCpuFlags(),
                    vds.getClusterCompatibilityVersion()));
        }

        getQueryReturnValue().setReturnValue(vds);
    }

    protected CpuFlagsManagerHandler getCpuFlagsManagerHandler() {
        return cpuFlagsManagerHandler;
    }
}
