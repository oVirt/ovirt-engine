package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
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
    protected void executeQueryCommand() {
        VDSBrokerFrontend vdsBroker = getVdsBroker();
        if (getDbFacade().getStorageDomainDao().get(getStorageDomainId()) == null) {
            getQueryReturnValue().setExceptionString(VdcBllMessages.STORAGE_DOMAIN_DOES_NOT_EXIST.toString());
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        // first, run getImagesList query into vdsm to get all of the images on the storage domain - then store in
        // imagesList
        VDSReturnValue imagesListResult = vdsBroker.RunVdsCommand(VDSCommandType.GetImagesList,
                new GetImagesListVDSCommandParameters(getStorageDomainId(), getStoragePoolId()));
        @SuppressWarnings("unchecked")
        List<Guid> imagesList = (List<Guid>) imagesListResult.getReturnValue();

        // fromDao is a list of all disk images on the domain from the DAO
        List<DiskImage> fromDao = getDbFacade().getDiskImageDao().getAllSnapshotsForStorageDomain(getStorageDomainId());

        // then, compare the list of all images on the domain with the list oVirt recognizes
        // if the ID in imagesList is recognized by oVirt, remove from list
        for (DiskImage image : fromDao) {
            imagesList.remove(image.getId());
        }
        List<Disk> unregisteredDisks = new ArrayList<Disk>();
        for (Guid unregisteredDiskId : imagesList) {
            GetUnregisteredDiskQueryParameters unregQueryParams = new GetUnregisteredDiskQueryParameters(
                    unregisteredDiskId, getStorageDomainId(), getStoragePoolId());
            VdcQueryReturnValue unregQueryReturn = runInternalQuery(VdcQueryType.GetUnregisteredDisk,
                    unregQueryParams);
            if (unregQueryReturn.getSucceeded()) {
                unregisteredDisks.add(unregQueryReturn.<Disk>getReturnValue());
            } else {
                log.debug("Could not get populated disk, reason: " + unregQueryReturn.getExceptionString());
            }
        }
        getQueryReturnValue().setReturnValue(unregisteredDisks);
    }

    protected VDSBrokerFrontend getVdsBroker() {
        return Backend.getInstance().getResourceManager();
    }

    protected Guid getStorageDomainId() {
        return getParameters().getStorageDomainId();
    }

    protected Guid getStoragePoolId() {
        return getParameters().getStoragePoolId();
    }
}
