package org.ovirt.engine.core.bll.qos;


import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public abstract class AddQosCommand<T extends QosBase, M extends QosValidator<T>> extends QosCommandBase<T, M> {

    public AddQosCommand(QosParametersBase<T> parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction() && validate(getQosValidator(getQos()).nameNotTakenInDc());
    }

    @Override
    protected void executeCommand() {
        getQos().setId(Guid.newGuid());
        getQosDao().save(getQos());
        getReturnValue().setActionReturnValue(getQos().getId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADDED_QOS : AuditLogType.USER_FAILED_TO_ADD_QOS;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }

}
