package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.GetAllAttachableDisksForVmQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllAttachableDisksForVmQuery<P extends GetAllAttachableDisksForVmQueryParameters> extends BaseGetAttachableDisksQuery<P> {

    public GetAllAttachableDisksForVmQuery(P parameters) {
        super(parameters);
    }

    protected List<Disk> filterDisks(List<Disk> diskList) {
        VM vm = DbFacade.getInstance().getVmDao().get(getParameters().getVmId(),
                getUserID(),
                getParameters().isFiltered());

        if (vm == null) {
            return new ArrayList<>();
        }

        return doFilter(diskList, vm.getOs(), vm.getVdsGroupCompatibilityVersion());
    }

}
