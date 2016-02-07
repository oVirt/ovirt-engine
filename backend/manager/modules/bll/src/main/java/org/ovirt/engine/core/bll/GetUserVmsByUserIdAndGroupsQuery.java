package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;

public class GetUserVmsByUserIdAndGroupsQuery<P extends GetUserVmsByUserIdAndGroupsParameters> extends QueriesCommandBase<P> {
    public GetUserVmsByUserIdAndGroupsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmList = getDbFacade().getVmDao().getAllForUserWithGroupsAndUserRoles(getUserID());
        for (VM vm : vmList) {
            updateVmGuestAgentVersion(vm);
            if (getParameters().getIncludeDiskData()) {
                updateDisksFromDB(vm);
                updateVmInit(vm);
                Collections.sort(vm.getDiskList(), new DiskByDiskAliasComparator());
                fillImagesBySnapshots(vm);
            }
        }
        setReturnValue(vmList);

    }

    protected void updateVmInit(VM vm) {
        VmHandler.updateVmInitFromDB(vm.getStaticData(), true);
    }

    protected void fillImagesBySnapshots(VM vm) {
        ImagesHandler.fillImagesBySnapshots(vm);
    }

    protected void updateDisksFromDB(VM vm) {
        VmHandler.updateDisksFromDb(vm);
    }

    protected void updateVmGuestAgentVersion(VM vm) {
        VmHandler.updateVmGuestAgentVersion(vm);
    }

}
