package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmStaticDao;

public class UpdateLabelCommand extends LabelCommandBase<LabelActionParameters> {

    @Inject
    private VmStaticDao vmStaticDao;

    public UpdateLabelCommand(LabelActionParameters parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        Label label = labelDao.get(getLabelId());
        Collection changedVms = CollectionUtils.disjunction(label.getVms(), getParameters().getLabel().getVms());
        vmStaticDao.incrementDbGenerationForVms(new ArrayList<>(changedVms));
        labelDao.update(getParameters().getLabel());
        setActionReturnValue(getParameters().getLabel());
        setSucceeded(true);
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

        if (label.isReadOnly() && !isInternalExecution()) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_READ_ONLY);
            return false;
        }

        label = getParameters().getLabel();

        if (label.getName() == null || label.getName().isEmpty()) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_BAD_NAME);
            return false;
        }

        Label existing = labelDao.getByName(label.getName());

        if (existing != null && !existing.getId().equals(label.getId())) {
            addValidationMessage(EngineMessage.AFFINITY_LABEL_NAME_ALREADY_EXISTS);
            return false;
        }

        return super.validate();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_AFFINITY_LABEL : AuditLogType.USER_FAILED_TO_UPDATE_AFFINITY_LABEL;
    }
}
