package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.errors.EngineMessage;

public abstract class UpdateQosCommandBase<T extends QosBase, M extends QosValidator<T>> extends QosCommandBase<T, M> {

    public UpdateQosCommandBase(QosParametersBase<T> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        M qosValidator = getQosValidator(getQos());
        return super.validate()
                && validate(qosValidator.qosExists())
                && validate(qosValidator.consistentDataCenter())
                && validate(qosValidator.nameNotChangedOrNotTaken());
    }

    @Override
    protected void executeCommand() {
        getQosDao().update(getQos());
        getReturnValue().setActionReturnValue(getQos().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_QOS : AuditLogType.USER_FAILED_TO_UPDATE_QOS;
    }
}
