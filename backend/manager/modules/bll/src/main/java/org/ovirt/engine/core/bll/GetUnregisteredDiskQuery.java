package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StoragePoolDomainAndGroupIdBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public class GetUnregisteredDiskQuery<P extends GetUnregisteredDiskQueryParameters> extends QueriesCommandBase<P> {

    public GetUnregisteredDiskQuery(P parameters) {
        super(parameters);
    }

    public GetUnregisteredDiskQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        Guid storagePoolId = getParameters().getStoragePoolId();
        Guid storageDomainId = getParameters().getStorageDomainId();
        Guid diskId = getParameters().getDiskId();
        if (getDbFacade().getStorageDomainDao().get(storageDomainId) == null) {
            getQueryReturnValue().setExceptionString(VdcBllMessages.STORAGE_DOMAIN_DOES_NOT_EXIST.toString());
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        // Now get the list of volumes for each new image.
        StoragePoolDomainAndGroupIdBaseVDSCommandParameters getVolumesParameters = new StoragePoolDomainAndGroupIdBaseVDSCommandParameters(
                storagePoolId, storageDomainId, diskId);
        VDSReturnValue volumesListReturn = getBackend().getResourceManager().RunVdsCommand(VDSCommandType.GetVolumesList,
                getVolumesParameters);
        if (!volumesListReturn.getSucceeded()) {
            getQueryReturnValue().setExceptionString(volumesListReturn.getExceptionString());
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        @SuppressWarnings("unchecked")
        List<Guid> volumesList = (List<Guid>) volumesListReturn.getReturnValue();

        // We can't deal with snapshots, so there should only be a single volume associated with the
        // image. If there are multiple volumes, skip the image and move on to the next.
        if (volumesList.size() != 1) {
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        Guid volumeId = volumesList.get(0);

        // Get the information about the volume from VDSM.
        GetImageInfoVDSCommandParameters imageInfoParameters = new GetImageInfoVDSCommandParameters(
                storagePoolId, storageDomainId, diskId, volumeId);
        VDSReturnValue imageInfoReturn = getBackend().getResourceManager().RunVdsCommand(
                VDSCommandType.GetImageInfo, imageInfoParameters);

        if (!imageInfoReturn.getSucceeded()) {
            getQueryReturnValue().setExceptionString(imageInfoReturn.getExceptionString());
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        DiskImage newDiskImage = (DiskImage) imageInfoReturn.getReturnValue();
        // The disk image won't have an interface set on it. Set it to IDE by default. When the
        // disk is attached to a VM, its interface can be changed to the appropriate value for that VM.
        newDiskImage.setDiskInterface(DiskInterface.IDE);
        newDiskImage.setStoragePoolId(storagePoolId);
        getQueryReturnValue().setReturnValue(newDiskImage);
        getQueryReturnValue().setSucceeded(true);
    }
}
