package org.ovirt.engine.core.bll;

import javax.inject.Inject;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;

public class GetVdsByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Inject
    private VdsDao vdsDao;

    @Override
    protected void executeQueryCommand() {
        VDS vds = vdsDao.get(getParameters().getId());

        if (vds != null) {
            vds.setCpuName(cpuFlagsManagerHandler.findMaxServerCpuByFlags(vds.getCpuFlags(),
                    vds.getClusterCompatibilityVersion()));
        }

        getQueryReturnValue().setReturnValue(vds);
    }
}
