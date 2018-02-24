package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.qos.QosDao;

public abstract class RemoveQosCommandBase<T extends QosBase, M extends QosValidator<T>, D extends QosDao<T>>
        extends QosCommandBase<T, M, D> {

    public RemoveQosCommandBase(QosParametersBase<T> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (!validateParameters()) {
            return false;
        }
        QosValidator<T> validator = getQosValidator(getQos());
        return super.validate() && validate(validator.qosExists()) && validate(validator.consistentDataCenter());
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
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVED_QOS : AuditLogType.USER_FAILED_TO_REMOVE_QOS;
    }
}
