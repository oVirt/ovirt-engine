package org.ovirt.engine.core.bll.network.host;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CpuFlagsManagerHandler;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;

public class GetHostsWithMissingFlagsForClusterQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VdsDao vdsDao;

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    public GetHostsWithMissingFlagsForClusterQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VDS> hostsOutOfSync = vdsDao
                .getAllForCluster(getParameters().getId())
                .stream()
                .filter(vds -> vds.getStatus() == VDSStatus.Up)
                .filter(vds -> hasMissingServerCpuFlags(vds))
                .collect(Collectors.toList());
            getQueryReturnValue().setReturnValue(hostsOutOfSync);
    }

    protected boolean hasMissingServerCpuFlags(VDS vds) {
        return cpuFlagsManagerHandler.missingServerCpuFlags(
                vds.getClusterCpuName(),
                vds.getCpuFlags(),
                vds.getClusterCompatibilityVersion()) != null;
    }
}
