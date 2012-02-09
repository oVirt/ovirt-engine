package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveParameters;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;

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
        RefObject<DiskImage> tempRefObject = new RefObject<DiskImage>(activeDisk);
        RefObject<DiskImage> tempRefObject2 = new RefObject<DiskImage>(inactiveDisk);
        int count = ImagesHandler.getImagesMappedToDrive(vmId, drive, tempRefObject, tempRefObject2);
        activeDisk = tempRefObject.argvalue;
        inactiveDisk = tempRefObject2.argvalue;
        if ((count == 0 || count > 2 || activeDisk == null || (count == 2 && inactiveDisk == null))) {
            log.warnFormat("Vm {0} images data incorrect", vmId);
            getQueryReturnValue().setReturnValue(new java.util.ArrayList<DiskImage>());
        } else {
            if (inactiveDisk != null) {
                tryingImage = activeDisk.getParentId();
            }
            Guid topmostImageGuid = inactiveDisk == null ? activeDisk.getId() : inactiveDisk.getId();

            getQueryReturnValue().setReturnValue(
                    ImagesHandler.getAllImageSnapshots(topmostImageGuid, activeDisk.getit_guid()));
            ((GetAllVmSnapshotsByDriveQueryReturnValue) getQueryReturnValue()).setTryingImage(tryingImage);
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(GetAllVmSnapshotsByDriveQuery.class);
}
