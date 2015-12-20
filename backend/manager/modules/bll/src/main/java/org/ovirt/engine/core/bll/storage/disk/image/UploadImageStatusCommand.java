package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UploadImageStatusParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;

public class UploadImageStatusCommand<T extends UploadImageStatusParameters> extends CommandBase<T> {

    public UploadImageStatusCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getUploadImageCommandId() == null
                && getParameters().getDiskId() == null) {
            log.error("Invalid parameters: command or disk id must be specified");
            setSucceeded(false);
        }

        ImageTransfer entity;
        if (getParameters().getUploadImageCommandId() != null) {
            entity = getDbFacade().getImageTransferDao().get(getParameters().getUploadImageCommandId());
        } else {
            entity = getDbFacade().getImageTransferDao().getByDiskId(getParameters().getDiskId());
        }

        if (entity != null) {
            // Always update; this serves as a keepalive
            entity = UploadImageCommand.updateEntity(getParameters().getUpdates(), entity.getId());
        } else {
            // Missing entity; this isn't unusual as the UI will poll until the entity is gone
            // due to upload completion or failure.  Instead of an error, we'll return an entity
            // with phase "UNKNOWN" and the UI will know what to do.
            if (getParameters().getUploadImageCommandId() != null) {
                log.info("UploadImageStatus request for missing or removed entity, command id {}",
                        getParameters().getUploadImageCommandId());
            } else {
                log.info("UploadImageStatus request for missing or removed entity, disk id {}",
                        getParameters().getDiskId());
            }

            entity = new ImageTransfer();
            entity.setId(getParameters().getUploadImageCommandId());
            entity.setPhase(ImageTransferPhase.UNKNOWN);
        }

        setSucceeded(true);
        getReturnValue().setActionReturnValue(entity);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Only check generic permissions because the command and/or ImageUpload entity may be missing
        return Collections.singletonList(new PermissionSubject(
                MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                ActionGroup.CREATE_DISK));
    }
}
