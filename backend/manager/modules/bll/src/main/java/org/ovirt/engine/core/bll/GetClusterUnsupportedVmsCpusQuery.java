package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetClusterUnsupportedVmsCpusParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The query checks if all of the VMs in a given cluster are compatible with a given cpu level.
 * The query returns the list of VMs+CPUs which are not compatible with the new CPU.
 */
public class GetClusterUnsupportedVmsCpusQuery<P extends GetClusterUnsupportedVmsCpusParameters> extends QueriesCommandBase<P> {
    public GetClusterUnsupportedVmsCpusQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmsForCluster = getDbFacade().getInstance().getVmDao().getAllForVdsGroup(getParameters().getVdsGroupId());

        Map<String, String> highCpuVms = new HashMap<>();
        for (VM vm : vmsForCluster) {
            if (vm.getCustomCpuName() != null && !vm.getCustomCpuName().isEmpty()) {
                String vmCpuName = CpuFlagsManagerHandler.getCpuNameByCpuId(vm.getCustomCpuName(), CpuFlagsManagerHandler.getLatestDictionaryVersion());
                if (vmCpuName == null || CpuFlagsManagerHandler.compareCpuLevels(vmCpuName, getParameters().getNewCpuName(), CpuFlagsManagerHandler.getLatestDictionaryVersion()) > 0) {
                    highCpuVms.put(vm.getName(), vm.getCustomCpuName());
                }
            }
        }
        getQueryReturnValue().setReturnValue(highCpuVms);
    }
}
