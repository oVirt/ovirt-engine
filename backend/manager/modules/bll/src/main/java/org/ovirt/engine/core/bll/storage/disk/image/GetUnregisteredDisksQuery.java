package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetUnregisteredDisksQuery<P extends GetUnregisteredDisksQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private DiskImageDao diskImageDao;

    public GetUnregisteredDisksQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        StorageDomain storageDomain = storageDomainDao.get(getStorageDomainId());
        if (storageDomain == null) {
            getQueryReturnValue().setExceptionString(EngineMessage.STORAGE_DOMAIN_DOES_NOT_EXIST.toString());
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        if (storageDomain.getStorageType().isCinderDomain()) {
            QueryReturnValue returnValue = runInternalQuery(QueryType.GetUnregisteredCinderDisksByStorageDomainId,
                    new IdQueryParameters(getStorageDomainId()));
            setReturnValue(returnValue.getReturnValue());
            return;
        } else if (!storageDomain.getStorageDomainType().isDataDomain()) {
            getQueryReturnValue().setExceptionString("Operation not allowed for non-data storage domains.");
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        // first, run getImagesList query into vdsm to get all of the images on the storage domain - then store in
        // imagesList
        VDSReturnValue imagesListResult = runVdsCommand(VDSCommandType.GetImagesList,
                new GetImagesListVDSCommandParameters(getStorageDomainId(), getStoragePoolId()));
        @SuppressWarnings("unchecked")
        List<Guid> imagesList = (List<Guid>) imagesListResult.getReturnValue();

        // fromDao is a list of all disk images on the domain from the Dao
        List<DiskImage> fromDao = diskImageDao.getAllSnapshotsForStorageDomain(getStorageDomainId());

        // then, compare the list of all images on the domain with the list oVirt recognizes
        // if the ID in imagesList is recognized by oVirt, remove from list
        for (DiskImage image : fromDao) {
            imagesList.remove(image.getId());
        }
        List<Disk> unregisteredDisks = new ArrayList<>();
        for (Guid unregisteredDiskId : imagesList) {
            GetUnregisteredDiskQueryParameters unregQueryParams = new GetUnregisteredDiskQueryParameters(
                    unregisteredDiskId, getStorageDomainId(), getStoragePoolId());
            QueryReturnValue unregQueryReturn = runInternalQuery(QueryType.GetUnregisteredDisk,
                    unregQueryParams);
            if (unregQueryReturn.getSucceeded()) {
                unregisteredDisks.add(unregQueryReturn.getReturnValue());
            } else {
                log.debug("Could not get populated disk: {}", unregQueryReturn.getExceptionString());
            }
        }
        getQueryReturnValue().setReturnValue(unregisteredDisks);
    }

    protected Guid getStorageDomainId() {
        return getParameters().getStorageDomainId();
    }

    protected Guid getStoragePoolId() {
        return getParameters().getStoragePoolId();
    }
}
