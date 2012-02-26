package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImagesComparerByName;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetUserVmsByUserIdAndGroupsQuery<P extends GetUserVmsByUserIdAndGroupsParameters>
        extends QueriesCommandBase<P> {
    public GetUserVmsByUserIdAndGroupsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (!shouldReturnValue()) {
            getQueryReturnValue().setReturnValue(new ArrayList<VM>());
        } else {
            List<VM> vmList =
                    DbFacade.getInstance().getVmDAO().getAllForUserWithGroupsAndUserRoles(getParameters().getId());
            for (VM vm : vmList) {
                VmHandler.UpdateVmGuestAgentVersion(vm);
                if (getParameters().getIncludeDiskData()) {
                    VmHandler.updateDisksFromDb(vm);
                    Collections.sort(vm.getDiskList(), new ImagesComparerByName());
                    for (DiskImage diskImage : vm.getDiskMap().values()) {
                        diskImage.getSnapshots().addAll(
                                ImagesHandler.getAllImageSnapshots(diskImage.getId(), diskImage.getit_guid()));
                    }
                }
            }
            getQueryReturnValue().setReturnValue(vmList);
        }
    }

    /**
     * Validates if the query should return anything or not, according to the user's permissions:
     * <ul>
     * <li>If the query is run as an administrator (note that since we've reached the {@link #executeQueryCommand()} method,
     * we've already validated that the use is indeed an administrator), the results from the database queries should be returned.</li>
     * <li>If the query is run as a user, it may return results <b>ONLY</b> if the user is querying about himself.</li>
     * </ul>
     */
    private boolean shouldReturnValue() {
        if (!getParameters().isFiltered()) {
            return true;
        }

        Guid executingUserID = getUserID();
        Guid requestedUserID = getParameters().getId();

        return executingUserID != null && requestedUserID != null && executingUserID.equals(requestedUserID);
    }
}
