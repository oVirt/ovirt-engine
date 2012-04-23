package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.GetAvailableClustersByServerCpuParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetAvailableClustersByServerCpuQuery<P extends GetAvailableClustersByServerCpuParameters>
        extends QueriesCommandBase<P> {
    public GetAvailableClustersByServerCpuQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // get all clusters
        List<VDSGroup> allClusters = DbFacade.getInstance().getVdsGroupDAO().getAll();
        // get all available cpu names

        String cpuName = getParameters().getCpuName();
        Version version = getParameters().getVersion();

        final List<String> availableCpus = LinqUtils.foreach(
                CpuFlagsManagerHandler.GetAllServerCpusBelowCpu(cpuName, version), new Function<ServerCpu, String>() {
                    @Override
                    public String eval(ServerCpu serverCpu) {
                        return serverCpu.getCpuName();
                    }
                });

        getQueryReturnValue().setReturnValue(LinqUtils.filter(allClusters, new Predicate<VDSGroup>() {
            @Override
            public boolean eval(VDSGroup vdsGroup) {
                return availableCpus.contains(vdsGroup.getcpu_name());
            }
        }));
    }
}
