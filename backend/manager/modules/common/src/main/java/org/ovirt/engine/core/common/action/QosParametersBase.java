package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.compat.Guid;

public class QosParametersBase<T extends QosBase> extends ActionParametersBase {

    private static final long serialVersionUID = 1304387921254822524L;

    @Valid
    private T qos;
    private Guid qosId;

    public T getQos() {
        return qos;
    }

    public void setQos(T qos) {
        this.qos = qos;
    }

    public Guid getQosId() {
        return qosId;
    }

    public void setQosId(Guid qosId) {
        this.qosId = qosId;
    }

}
