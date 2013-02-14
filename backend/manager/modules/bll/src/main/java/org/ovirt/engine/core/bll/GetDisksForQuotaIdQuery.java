package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetDisksByQuotaIdParameters;

public class GetDisksForQuotaIdQuery<P extends GetDisksByQuotaIdParameters> extends QueriesCommandBase<P> {
    public GetDisksForQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<DiskImage> disks = getDbFacade().getDiskImageDao().getAllForQuotaId(getParameters().getQuotaId());
        for (DiskImage diskImage : disks) {
            diskImage.getSnapshots().addAll(
                    ImagesHandler.getAllImageSnapshots(diskImage.getImageId(), diskImage.getImageTemplateId()));
        }
        getQueryReturnValue().setReturnValue(disks);
    }
}
