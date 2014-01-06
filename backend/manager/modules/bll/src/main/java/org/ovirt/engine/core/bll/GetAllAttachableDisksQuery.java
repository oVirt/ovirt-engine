package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.GetAllAttachableDisks;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.springframework.util.CollectionUtils;

public class GetAllAttachableDisksQuery<P extends GetAllAttachableDisks> extends QueriesCommandBase<P> {

    public GetAllAttachableDisksQuery(P parameters) {
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
            setReturnValue(new ArrayList<>());
            return;
        }

        VM vm = DbFacade.getInstance().getVmDao().get(getParameters().getVmId(),
                getUserID(),
                getParameters().isFiltered());
        if (vm == null) {
            setReturnValue(new ArrayList<>());
            return;
        }

        List<Disk> filteredDiskList = new ArrayList<>();
        for (Disk disk : diskList) {
            if (VmValidationUtils.isDiskInterfaceSupportedByOs(vm.getOs(),
                    vm.getVdsGroupCompatibilityVersion(),
                    disk.getDiskInterface())) {
                filteredDiskList.add(disk);
            }
        }

        setReturnValue(filteredDiskList);
    }

    protected OsRepository getOsRepository() {
        return SimpleDependecyInjector.getInstance().get(OsRepository.class);
    }

}
