package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveParameters;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@Deprecated
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
        List<DiskImage> images =
                DbFacade.getInstance().getDiskImageDAO().getAllForVm(vmId, getUserID(), getParameters().isFiltered());
        int count = ImagesHandler.getImagesMappedToDrive(images, drive, refActive, refInactive);
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

            // Note that no additional permission filtering is needed -
            // if a user could read the disk of a VM, all its snapshots are OK too
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
