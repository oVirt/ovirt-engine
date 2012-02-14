package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveParameters;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;

public class GetAllVmSnapshotsByDriveQuery<P extends GetAllVmSnapshotsByDriveParameters>
        extends QueriesCommandBase<P> {
    public GetAllVmSnapshotsByDriveQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid tryingImage = Guid.Empty;
        Guid vmId = getParameters().getId();
        String drive = getParameters().getDrive();
        DiskImage inactiveDisk = null;
        DiskImage activeDisk = null;
        RefObject<DiskImage> refActive = new RefObject<DiskImage>(activeDisk);
        RefObject<DiskImage> refInactive = new RefObject<DiskImage>(inactiveDisk);
        int count = ImagesHandler.getImagesMappedToDrive(vmId, drive, refActive, refInactive);
        activeDisk = refActive.argvalue;
        inactiveDisk = refInactive.argvalue;
        if ((count == 0 || count > 2 || activeDisk == null || (count == 2 && inactiveDisk == null))) {
            log.warnFormat("Vm {0} images data incorrect", vmId);
            getQueryReturnValue().setReturnValue(new ArrayList<DiskImage>());
        } else {
            if (inactiveDisk != null) {
                tryingImage = activeDisk.getParentId();
            }
            Guid topmostImageGuid = inactiveDisk == null ? activeDisk.getId() : inactiveDisk.getId();

            getQueryReturnValue().setReturnValue(
                    ImagesHandler.getAllImageSnapshots(topmostImageGuid, activeDisk.getit_guid()));
            getQueryReturnValue().setTryingImage(tryingImage);
        }
    }

    @Override
    public GetAllVmSnapshotsByDriveQueryReturnValue getQueryReturnValue() {
        return (GetAllVmSnapshotsByDriveQueryReturnValue) super.getQueryReturnValue();
    }
}
