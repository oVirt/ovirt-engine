package org.ovirt.engine.core.bll.qos;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.qos.QosDao;


public abstract class QosCommandBase<T extends QosBase, M extends QosValidator<T>> extends CommandBase<QosParametersBase<T>> {

    private T qos;
    private Guid qosId;

    public QosCommandBase(QosParametersBase<T> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        if (getQos() != null) {
            setStoragePoolId(getQos().getStoragePoolId());
            addCustomValue("QosName", getQos().getName());
        }
        getParameters().setShouldBeLogged(true);
    }

    @Override
    protected boolean validate() {
        M validator = getQosValidator(getQos());
        return validateParameters()
                && validate(validator.requiredValuesPresent());
    }

    public T getQos() {
        if (qos == null) {
            if (getParameters().getQos() == null) {
                if (getParameters().getQosId() != null) {
                    qos = getQosDao().get(getParameters().getQosId());
                }
            } else {
                qos = getParameters().getQos();
            }
        }
        return qos;
    }

    public Guid getQosId() {
        if (qosId == null) {
            if (getParameters().getQosId() != null) {
                qosId = getParameters().getQosId();
            } else if (getParameters().getQos() != null) {
                qosId = getParameters().getQos().getId();
            }
        }
        return qosId;
    }

    @Override
    public Guid getStoragePoolId() {
        if (super.getStoragePoolId() == null && getQos() != null) {
            setStoragePoolId(getQos().getStoragePoolId());
        }
        return super.getStoragePoolId();
    }

    protected boolean validateParameters() {
        if (getQos() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_QOS_NOT_FOUND);
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }

    protected abstract QosDao<T> getQosDao();

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__QOS);
    }

    protected abstract M getQosValidator(T qosBase);

}
