package org.ovirt.engine.core.bll.qos;


import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public abstract class UpdateQosCommandBase<T extends QosBase, M extends QosValidator<T>> extends QosCommandBase<T, M> {

    public UpdateQosCommandBase(QosParametersBase<T> parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        M qosValidator = getQosValidator(getQos());
        return super.canDoAction()
                && validate(qosValidator.qosExists())
                && validate(qosValidator.consistentDataCenter());
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
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_QOS : AuditLogType.USER_FAILED_TO_UPDATE_QOS;
    }
}
