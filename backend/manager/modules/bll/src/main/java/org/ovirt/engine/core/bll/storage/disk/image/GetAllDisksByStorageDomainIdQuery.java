package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllDisksByStorageDomainIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllDisksByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    public GetAllDisksByStorageDomainIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        StorageDomain storageDomain = getDbFacade().getStorageDomainDao().get(getParameters().getId());
        if (storageDomain.getStorageType().isCinderDomain()) {
            List<DiskImage> diskImages = getDbFacade().getDiskImageDao().getAllForStorageDomain(getParameters().getId());
            getQueryReturnValue().setReturnValue(diskImages);
        } else {
            List<DiskImage> diskImages =
                    getDbFacade().getDiskImageDao().getAllSnapshotsForStorageDomain(getParameters().getId());

            Map<Guid, DiskImage> diskImagesMap = new HashMap<>();

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

            getQueryReturnValue().setReturnValue(new ArrayList<>(diskImagesMap.values()));
        }
    }
}
