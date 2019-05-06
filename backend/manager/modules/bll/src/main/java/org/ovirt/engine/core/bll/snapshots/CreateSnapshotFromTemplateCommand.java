package org.ovirt.engine.core.bll.snapshots;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotFromTemplateParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;

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
@NonTransactiveCommandAttribute
public class CreateSnapshotFromTemplateCommand<T extends CreateSnapshotFromTemplateParameters> extends
        CreateSnapshotCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;

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
            //TODO: removeImage() is under that condition as it  will perform only if the disk exits in the db.
            // The flow should be changed so that the disk is added in transaction and then the copy flow is initiated.
            // The disk should be removed from the db only in case of successful removal (otherwise it should remain
            // illegal to let the user attempt to delete it again).
            removeImage();
            baseDiskDao.remove(getDestinationDiskImage().getId());
            if (diskImageDynamicDao.get(getDestinationDiskImage().getImageId()) != null) {
                diskImageDynamicDao.remove(getDestinationDiskImage().getImageId());
            }
        }

        super.endWithFailure();
    }

    private void removeImage() {
        RemoveImageParameters removeImageParams =
                new RemoveImageParameters(getParameters().getDestinationImageId());
        removeImageParams.setStorageDomainId(getDestinationStorageDomainId());
        removeImageParams.setDbOperationScope(ImageDbOperationScope.NONE);
        removeImageParams.setShouldLockImage(false);
        removeImageParams.setEntityInfo(new EntityInfo(VdcObjectType.Disk, getDestinationDiskImage().getId()));
        ActionReturnValue returnValue = runInternalActionWithTasksContext(
                ActionType.RemoveImage,
                removeImageParams);
        if (!returnValue.getSucceeded()) {
            addAuditLogOnRemoveFailure();
        }
    }

    private void addAuditLogOnRemoveFailure() {
        addCustomValue("DiskAlias", getParameters().getDiskAlias());
        AuditLogType logType = AuditLogType.USER_COPY_IMAGE_GROUP_FAILED_TO_DELETE_DST_IMAGE;
        auditLogDirector.log(this, logType);
    }

    @Override
    protected void revertTasks() {
    }
}
