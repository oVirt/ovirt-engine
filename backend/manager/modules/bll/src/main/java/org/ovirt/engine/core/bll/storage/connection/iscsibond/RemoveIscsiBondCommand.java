package org.ovirt.engine.core.bll.storage.connection.iscsibond;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.IscsiBondValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveIscsiBondParameters;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class RemoveIscsiBondCommand<T extends RemoveIscsiBondParameters> extends BaseIscsiBondCommand<T> {
    @Inject
    private IscsiBondValidator validator;

    private IscsiBond iscsiBond;

    public RemoveIscsiBondCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        return validate(validator.isIscsiBondExist(getIscsiBond()));
    }

    @Override
    protected void executeCommand() {
        iscsiBondDao.remove(getParameters().getIscsiBondId());
        setSucceeded(true);
    }

    @Override
    public Guid getStoragePoolId() {
        Guid storagePoolId = super.getStoragePoolId();

        if (storagePoolId == null) {
            IscsiBond iscsiBond = getIscsiBond();

            if (iscsiBond != null) {
                storagePoolId = iscsiBond.getStoragePoolId();
                setStoragePoolId(storagePoolId);
            }
        }

        return storagePoolId;
    }

    @Override
    protected IscsiBond getIscsiBond() {
        if (iscsiBond == null) {
            iscsiBond = iscsiBondDao.get(getParameters().getIscsiBondId());
        }

        return iscsiBond;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.ISCSI_BOND_REMOVE_SUCCESS : AuditLogType.ISCSI_BOND_REMOVE_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__ISCSI_BOND);
    }
}
