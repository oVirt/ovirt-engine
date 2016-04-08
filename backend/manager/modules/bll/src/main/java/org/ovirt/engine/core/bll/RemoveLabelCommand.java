package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LabelActionParametersBase;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveLabelCommand<T extends LabelActionParametersBase> extends LabelCommandBase<T> {

    public RemoveLabelCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (getLabelId() != null) {
            labelDao.remove(getLabelId());
            setSucceeded(true);
        }
    }

    @Override
    protected boolean validate() {
        if (getLabelId() == null) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_NOT_EXISTS);
            return false;
        }

        Label label = labelDao.get(getLabelId());

        if (label == null) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_NOT_EXISTS);
            return false;
        }

        if (label.isReadOnly()) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_READ_ONLY);
            return false;
        }

        if (!label.getVms().isEmpty()) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_NOT_EMPTY);
            return false;
        }

        if (!label.getHosts().isEmpty()) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_NOT_EMPTY);
            return false;
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVED_AFFINITY_LABEL : AuditLogType.USER_FAILED_TO_REMOVE_AFFINITY_LABEL;
    }
}
