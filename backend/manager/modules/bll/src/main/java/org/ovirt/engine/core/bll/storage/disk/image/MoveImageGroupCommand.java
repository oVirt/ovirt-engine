package org.ovirt.engine.core.bll.storage.disk.image;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ImageDao;

@SuppressWarnings("serial")
@InternalCommandAttribute
public class MoveImageGroupCommand<T extends MoveOrCopyImageGroupParameters> extends CopyImageGroupCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ImageDao imageDao;

    public MoveImageGroupCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private void removeImage(Guid storageDomainId) {
        RemoveImageParameters removeImageParams =
                new RemoveImageParameters(getParameters().getImageId());
        removeImageParams.setStorageDomainId(storageDomainId);
        removeImageParams.setParentCommand(ActionType.RemoveImage);
        removeImageParams.setDbOperationScope(ImageDbOperationScope.NONE);
        removeImageParams.setShouldLockImage(false);
        removeImageParams.setCorrelationId(getParameters().getCorrelationId());
        //TODO: should be removed as async task manager issues would be resolved, done in order
        // to avoid operations on the image to wait for this remove operation as currently async
        // task manager calls the end methods of command based on the entity id.
        // the remove done here is a "clenaup", either on the source domain or on the target - so
        // other operations on the image shouldn't be dependent and wait for it.
        removeImageParams.setEntityInfo(new EntityInfo(VdcObjectType.Disk, Guid.newGuid()));
        ActionReturnValue returnValue = runInternalAction(
                ActionType.RemoveImage,
                removeImageParams,
                cloneContextAndDetachFromParent());
        if (returnValue.getSucceeded()) {
            startPollingAsyncTasks(returnValue.getInternalVdsmTaskIdList());
        } else {
            addAuditLogOnRemoveFailure();
        }
    }

    @Override
    protected void endSuccessfully() {
        removeImage(getParameters().getSourceDomainId());
        super.endSuccessfully();
    }

    @Override
    protected void endWithFailure() {
        removeImage(getParameters().getStorageDomainId());
        unLockImage();
    }

    private void addAuditLogOnRemoveFailure() {
        addCustomValue("DiskAlias", getDiskImage().getDiskAlias());
        addCustomValue("StorageDomainName", getStorageDomain().getStorageName());
        AuditLogType logType = null;
        if (getActionState() == CommandActionState.END_SUCCESS) {
            logType = AuditLogType.USER_MOVE_IMAGE_GROUP_FAILED_TO_DELETE_SRC_IMAGE;
        } else {
            logType = AuditLogType.USER_MOVE_IMAGE_GROUP_FAILED_TO_DELETE_DST_IMAGE;
        }
        auditLogDirector.log(this, logType);
    }
}
