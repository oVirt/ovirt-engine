package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.TransferImageStatusParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageTransferDao;

@NonTransactiveCommandAttribute
public class TransferImageStatusCommand<T extends TransferImageStatusParameters> extends CommandBase<T> {

    @Inject
    private ImageTransferDao imageTransferDao;

    @Inject
    private ImageTransferUpdater imageTransferUpdater;

    private ImageTransfer entity;

    public TransferImageStatusCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getParameters().getTransferImageCommandId() == null
                && getParameters().getDiskId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_TRANSFER_IMAGE_STATUS_MISSING_PARAM);
        }

        if (getParameters().getTransferImageCommandId() != null) {
            entity = imageTransferDao.get(getParameters().getTransferImageCommandId());
        } else {
            entity = imageTransferDao.getByDiskId(getParameters().getDiskId());
        }

        ImageTransfer updates = getParameters().getUpdates();
        if (updates != null && entity != null && updates.getPhase() != null) {
            ImageTransferPhase imageTransferPhaseUpdate = updates.getPhase();

            switch (imageTransferPhaseUpdate) {
                case RESUMING:
                    // Can resume only a paused transfer.
                    if (!entity.getPhase().isPaused()) {
                        return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_RESUME_IMAGE_TRANSFER);
                    }
                    break;
                case PAUSED_SYSTEM:
                case PAUSED_USER:
                    // Can pause only an upload.
                    // Note: should be kept correlated with any 'PAUSED_*' enum key (ImageTransferPhase:isPaused)
                    if (entity.getType() == TransferType.Download) {
                        return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_PAUSE_IMAGE_DOWNLOAD);
                    }
                    break;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        if (entity != null) {
            // Always update; this serves as a keepalive
            entity = imageTransferUpdater.updateEntity(getParameters().getUpdates(), entity.getId(), false);
        } else {
            // Missing entity; this isn't unusual as the UI will poll until the entity is gone
            // due to upload completion or failure.  Instead of an error, we'll return an entity
            // with phase "UNKNOWN" and the UI will know what to do.
            if (getParameters().getTransferImageCommandId() != null) {
                log.info("TransferImageStatus request for missing or removed entity, command id {}",
                        getParameters().getTransferImageCommandId());
            } else {
                log.info("TransferImageStatus request for missing or removed entity, disk id {}",
                        getParameters().getDiskId());
            }

            entity = new ImageTransfer();
            entity.setId(getParameters().getTransferImageCommandId());
            entity.setPhase(ImageTransferPhase.UNKNOWN);
            entity.setType(TransferType.Unknown);
            entity.setActive(false);
        }

        setSucceeded(true);
        getReturnValue().setActionReturnValue(entity);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid objectId = getStorageDomainId();
        if (objectId == null) {
            objectId = getParameters().getStorageDomainId();
            if (objectId != null) {
                setStorageDomainId(objectId);
            } else {
                objectId = MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID;
            }
        }

        // Only check generic permissions because the command and/or ImageUpload entity may be missing
        return Collections.singletonList(new PermissionSubject(
                objectId,
                VdcObjectType.System,
                ActionGroup.CREATE_DISK));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        // An AuditLogType message is appended to the params when an error occurs.
        if (getParameters().getAuditLogType() != null) {
            addCustomValue("DiskId", getParameters().getDiskId().toString());
            addCustomValue("EngineUrl", getParameters().getProxyLocation());
            return getParameters().getAuditLogType();
        } else {
            return super.getAuditLogTypeValue();
        }
    }
}
