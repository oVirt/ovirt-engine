package org.ovirt.engine.core.bll.snapshots;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.CreateSnapshotFromTemplateParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

/**
 * This command responsible to creating new snapshot. Usually it will be called
 * during new vm creation. In the case of create snapshot from template new
 * image created from master image aka image template so new created image
 * it_guid will be equal to master image guid.
 *
 * Parameters: Guid imageId - id of ImageTemplate, snapshot will be created from
 * Guid containerId - id of VmTemplate, contains ImageTemplate
 */

@InternalCommandAttribute
public class CreateSnapshotFromTemplateCommand<T extends CreateSnapshotFromTemplateParameters> extends
        CreateSnapshotCommand<T> {

    public CreateSnapshotFromTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        super.setVmId(parameters.getVmId());
        setImageGroupId(Guid.newGuid());
    }

    public CreateSnapshotFromTemplateCommand(Guid guid) {
        super(guid);
    }

    /**
     * Old image not have to be changed
     */
    @Override
    protected void processOldImageFromDb() {
    }

    @Override
    protected DiskImage cloneDiskImage(Guid newImageGuid) {
        DiskImage returnValue = super.cloneDiskImage(newImageGuid);
        returnValue.setImageTemplateId(getImage().getImageId());
        return returnValue;
    }

    @Override
    protected Guid getDestinationStorageDomainId() {
        Guid storageDomainId = getParameters().getDestStorageDomainId();
        if (getParameters().getDestinationImageId() == null
                || Guid.Empty.equals(getParameters().getDestStorageDomainId())) {
            storageDomainId = getParameters().getStorageDomainId();
        }
        storageDomainId = (storageDomainId == null) ? Guid.Empty : storageDomainId;
        return !Guid.Empty.equals(storageDomainId) ? storageDomainId : super.getDestinationStorageDomainId();
    }

    @Override
    protected void endWithFailure() {
        if (getDestinationDiskImage() != null) {
            baseDiskDao.remove(getDestinationDiskImage().getId());
            if (diskImageDynamicDao.get(getDestinationDiskImage().getImageId()) != null) {
                diskImageDynamicDao.remove(getDestinationDiskImage().getImageId());
            }
        }

        super.endWithFailure();
    }
}
