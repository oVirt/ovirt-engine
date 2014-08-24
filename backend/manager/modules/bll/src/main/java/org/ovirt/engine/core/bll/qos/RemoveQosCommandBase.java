package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public abstract class RemoveQosCommandBase<T extends QosBase, M extends QosValidator<T>> extends QosCommandBase<T, M> {

    public RemoveQosCommandBase(QosParametersBase<T> parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (!validateParameters()) {
            return false;
        }
        QosValidator<T> validator = getQosValidator(getQos());
        return super.canDoAction() && validate(validator.qosExists()) && validate(validator.consistentDataCenter());
    }

    @Override
    protected void executeCommand() {
        getQosDao().remove(getQos().getId());
        getReturnValue().setActionReturnValue(getQos().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVED_QOS : AuditLogType.USER_FAILED_TO_REMOVE_QOS;
    }
}
