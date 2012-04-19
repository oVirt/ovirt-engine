package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetDisksByQuotaIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetDisksForQuotaIdQuery<P extends GetDisksByQuotaIdParameters> extends QueriesCommandBase<P> {
    public GetDisksForQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<DiskImage> disks = DbFacade.getInstance().getDiskImageDAO().getAllForQuotaId(getParameters().getQuotaId());
        for (DiskImage diskImage : disks) {
            diskImage.getSnapshots().addAll(
                    ImagesHandler.getAllImageSnapshots(diskImage.getImageId(), diskImage.getit_guid()));
        }
        getQueryReturnValue().setReturnValue(disks);
    }
}
