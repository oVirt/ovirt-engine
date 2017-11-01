package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmStaticDao;

public class AddLabelCommand<T extends LabelActionParameters> extends LabelCommandBase<T> {

    @Inject
    private VmStaticDao vmStaticDao;

    public AddLabelCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        Label label = new LabelBuilder(getParameters().getLabel())
                .build();

        labelDao.save(label);
        vmStaticDao.incrementDbGenerationForVms(new ArrayList<Guid>(label.getVms()));
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

        if (getParameters().getLabel().isReadOnly() && !isInternalExecution()) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_READ_ONLY);
            return false;
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADDED_AFFINITY_LABEL : AuditLogType.USER_FAILED_TO_ADD_AFFINITY_LABEL;
    }

    @Override
    protected void setAuditCustomValues() {
        addCustomValue("labelName", getParameters().getLabel().getName());
    }
}
