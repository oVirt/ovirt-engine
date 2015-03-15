package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.GetAllAttachableDisksForVmQueryParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseGetAttachableDisksQuery<P extends GetAllAttachableDisksForVmQueryParameters> extends QueriesCommandBase<P> {

    public BaseGetAttachableDisksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Disk> diskList = DbFacade.getInstance()
                .getDiskDao()
                .getAllAttachableDisksByPoolId(getParameters().getStoragePoolId(),
                        getParameters().getVmId(),
                        getUserID(),
                        getParameters().isFiltered());
        if (CollectionUtils.isEmpty(diskList)) {
            setReturnValue(diskList);
            return;
        }

        setReturnValue(filterDisks(diskList));
    }

    protected abstract List<Disk> filterDisks(List<Disk> diskList);

    protected List<Disk> doFilter(List<Disk> diskList, int osId, Version clusterVersion) {
        List<Disk> filteredDiskList = new ArrayList<>();
        for (Disk disk : diskList) {
            if (VmValidationUtils.isDiskInterfaceSupportedByOs(osId,
                    clusterVersion,
                    disk.getDiskInterface())) {
                filteredDiskList.add(disk);
            }
        }
        return filteredDiskList;
    }

}
