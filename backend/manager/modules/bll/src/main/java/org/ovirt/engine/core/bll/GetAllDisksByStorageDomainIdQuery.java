package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GetAllDisksByStorageDomainIdQuery<P extends StorageDomainQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllDisksByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<DiskImage> diskImages =
                getDbFacade().getDiskImageDao().getAllSnapshotsForStorageDomain(getParameters().getStorageDomainId());

        Map<Guid, DiskImage> diskImagesMap = new HashMap<Guid, DiskImage>();

        // Get active diskImages
        for (DiskImage diskImage : diskImages) {
            if (diskImage.getActive()) {
                diskImage.getSnapshots().add(DiskImage.copyOf(diskImage));
                diskImagesMap.put(diskImage.getId(), diskImage);
            }
        }

        // Update diskImages' snapshots
        for (DiskImage diskImage : diskImages) {
            if (!diskImage.getActive()) {
                DiskImage activeImage = diskImagesMap.get(diskImage.getId());
                if (activeImage != null) {
                    activeImage.getSnapshots().add(diskImage);
                }
            }
        }

        getQueryReturnValue().setReturnValue(new ArrayList<DiskImage>(diskImagesMap.values()));
    }
}
