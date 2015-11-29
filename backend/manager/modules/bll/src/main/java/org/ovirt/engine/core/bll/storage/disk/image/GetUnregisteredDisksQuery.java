package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public class GetUnregisteredDisksQuery<P extends GetUnregisteredDisksQueryParameters> extends QueriesCommandBase<P> {

    public GetUnregisteredDisksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected BackendInternal getBackend() {
        return super.getBackend();
    }

    @Override
    protected void executeQueryCommand() {
        StorageDomain storageDomain = getDbFacade().getStorageDomainDao().get(getStorageDomainId());
        if (storageDomain == null) {
            getQueryReturnValue().setExceptionString(EngineMessage.STORAGE_DOMAIN_DOES_NOT_EXIST.toString());
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        if (storageDomain.getStorageType().isCinderDomain()) {
            VdcQueryReturnValue returnValue = runInternalQuery(VdcQueryType.GetUnregisteredCinderDisksByStorageDomainId,
                    new IdQueryParameters(getStorageDomainId()));
            setReturnValue(returnValue.getReturnValue());
            return;
        }

        // first, run getImagesList query into vdsm to get all of the images on the storage domain - then store in
        // imagesList
        VDSReturnValue imagesListResult = runVdsCommand(VDSCommandType.GetImagesList,
                new GetImagesListVDSCommandParameters(getStorageDomainId(), getStoragePoolId()));
        @SuppressWarnings("unchecked")
        List<Guid> imagesList = (List<Guid>) imagesListResult.getReturnValue();

        // fromDao is a list of all disk images on the domain from the Dao
        List<DiskImage> fromDao = getDbFacade().getDiskImageDao().getAllSnapshotsForStorageDomain(getStorageDomainId());

        // then, compare the list of all images on the domain with the list oVirt recognizes
        // if the ID in imagesList is recognized by oVirt, remove from list
        for (DiskImage image : fromDao) {
            imagesList.remove(image.getId());
        }
        List<Disk> unregisteredDisks = new ArrayList<>();
        for (Guid unregisteredDiskId : imagesList) {
            GetUnregisteredDiskQueryParameters unregQueryParams = new GetUnregisteredDiskQueryParameters(
                    unregisteredDiskId, getStorageDomainId(), getStoragePoolId());
            VdcQueryReturnValue unregQueryReturn = runInternalQuery(VdcQueryType.GetUnregisteredDisk,
                    unregQueryParams);
            if (unregQueryReturn.getSucceeded()) {
                unregisteredDisks.add(unregQueryReturn.<Disk>getReturnValue());
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
