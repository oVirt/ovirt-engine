package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparerByDiskSize;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class GetVmsByStorageDomainQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetVmsByStorageDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid domainId = getParameters().getId();
        List<DiskImage> disksOfDomain = null;

        List<VM> vms = getAllVMsForStorageDomain(domainId);

        // get all disks and snapshots
        VdcQueryReturnValue queryReturnValue = getAllDisksByStorageDomain(domainId);
        if (queryReturnValue.getSucceeded()) {
            disksOfDomain = queryReturnValue.getReturnValue();
            Map<String, VM> vmNameToVMMap = new HashMap<>();

            for (VM vm : vms) {
                vmNameToVMMap.put(vm.getName(), vm);
            }

            for (DiskImage disk : disksOfDomain) {
                List<String> vmNames = disk.getVmNames();
                if (vmNames == null) {
                    continue;
                }
                for (String vmName : vmNames) {
                    VM vm = vmNameToVMMap.get(vmName);
                    if (vm != null) {
                        vm.getDiskMap().put(disk.getId(), disk);
                        vm.getDiskList().add(disk);
                    }
                }

            }
            Collections.sort(vms, Collections.reverseOrder(new VmsComparerByDiskSize()));
            getQueryReturnValue().setReturnValue(vms);
        }
        else {
            log.error("Failed to retrieve disks by storage domain id '{}': {}",
                    domainId,
                    queryReturnValue.getExceptionString());
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString(queryReturnValue.getExceptionString());
        }

    }

    protected VdcQueryReturnValue getAllDisksByStorageDomain(Guid domainId) {
        return runInternalQuery(VdcQueryType.GetAllDisksByStorageDomainId,
                new IdQueryParameters(domainId));
    }

    protected List<VM> getAllVMsForStorageDomain(Guid domainId) {
        return getDbFacade().getVmDao().getAllForStorageDomain(domainId);
    }

}
