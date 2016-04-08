package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class AddLabelCommand<T extends LabelActionParameters> extends LabelCommandBase<T> {

    public AddLabelCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        Label label = new LabelBuilder(getParameters().getLabel())
                .build();

        labelDao.save(label);

        setSucceeded(true);
        setActionReturnValue(label.getId());
    }

    @Override
    protected boolean validate() {
        Label label = labelDao.get(getLabelId());

        if (label != null) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_ID_ALREADY_EXISTS);
            return false;
        }

        if (getParameters().getLabel().getName().isEmpty()) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_BAD_NAME);
            return false;
        }

        label = labelDao.getByName(getParameters().getLabel().getName());

        if (label != null) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_NAME_ALREADY_EXISTS);
            return false;
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADDED_AFFINITY_LABEL : AuditLogType.USER_FAILED_TO_ADD_AFFINITY_LABEL;
    }
}
