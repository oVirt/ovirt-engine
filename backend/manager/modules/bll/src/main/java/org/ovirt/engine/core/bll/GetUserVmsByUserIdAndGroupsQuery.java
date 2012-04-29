package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ImagesComparerByName;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;

public class GetUserVmsByUserIdAndGroupsQuery<P extends GetUserVmsByUserIdAndGroupsParameters> extends GetDataByUserIDQueriesBase<P> {
    public GetUserVmsByUserIdAndGroupsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<VM> getPrivilegedQueryReturnValue() {
        List<VM> vmList = getDbFacade().getVmDAO().getAllForUserWithGroupsAndUserRoles(getParameters().getUserId());
        for (VM vm : vmList) {
            VmHandler.UpdateVmGuestAgentVersion(vm);
            if (getParameters().getIncludeDiskData()) {
                VmHandler.updateDisksFromDb(vm);
                Collections.sort(vm.getDiskList(), new ImagesComparerByName());
                ImagesHandler.fillImagesBySnapshots(vm);
            }
        }
        return vmList;
    }
}
