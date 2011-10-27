package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetAllDisksByVmIdQuery<P extends GetAllDisksByVmIdParameters> extends QueriesCommandBase<P> {
    public GetAllDisksByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // LINQ
        // DbFacade.Instance.GetImagesByVmGuid(GetParameters.VmId).Where(image=>image.active
        // == true).ToList();
        List<DiskImage> disks =
                LinqUtils.filter(
                        DbFacade.getInstance().getDiskImageDAO().getAllForVm(getParameters().getVmId()),
                        new Predicate<DiskImage>() {
                            @Override
                            public boolean eval(DiskImage diskImage) {
                                return (diskImage.getactive());
                            }
                        });
        for (DiskImage diskImage : disks) {
            diskImage.getSnapshots().addAll(
                    ImagesHandler.getAllImageSnapshots(diskImage.getId(), diskImage.getit_guid()));

        }
        getQueryReturnValue().setReturnValue(disks);
    }
}
