package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmsByStorageDomainQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    public GetVmsByStorageDomainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid domainId = getParameters().getId();

        List<VM> vms = getAllVMsForStorageDomain(domainId);

        // get all disks and snapshots
        QueryReturnValue queryReturnValue = getAllDisksByStorageDomain(domainId);
        if (queryReturnValue.getSucceeded()) {
            List<DiskImage> disksOfDomain = queryReturnValue.getReturnValue();
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
            Collections.sort(vms, Comparator.comparingDouble(VM::getActualDiskWithSnapshotsSize).reversed());
            getQueryReturnValue().setReturnValue(vms);
        } else {
            log.error("Failed to retrieve disks by storage domain id '{}': {}",
                    domainId,
                    queryReturnValue.getExceptionString());
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString(queryReturnValue.getExceptionString());
        }

    }

    protected QueryReturnValue getAllDisksByStorageDomain(Guid domainId) {
        return runInternalQuery(QueryType.GetAllDisksByStorageDomainId,
                new IdQueryParameters(domainId));
    }

    protected List<VM> getAllVMsForStorageDomain(Guid domainId) {
        return vmDao.getAllForStorageDomain(domainId);
    }

}
