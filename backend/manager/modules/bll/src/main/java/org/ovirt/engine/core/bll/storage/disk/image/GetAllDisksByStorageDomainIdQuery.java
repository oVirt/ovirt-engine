package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetAllDisksByStorageDomainIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ImagesHandler imagesHandler;

    public GetAllDisksByStorageDomainIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        StorageDomain storageDomain = storageDomainDao.get(getParameters().getId());
        if (storageDomain.getStorageType().isCinderDomain()) {
            List<DiskImage> diskImages = diskImageDao.getAllForStorageDomain(getParameters().getId());
            getQueryReturnValue().setReturnValue(diskImages);
        } else {
            List<DiskImage> diskImages = diskImageDao.getAllSnapshotsForStorageDomain(getParameters().getId());
            getQueryReturnValue().setReturnValue(new ArrayList<>(imagesHandler.aggregateDiskImagesSnapshots(diskImages)));
        }
    }
}
