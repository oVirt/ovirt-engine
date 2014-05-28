package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;


/**
 * Parameter class for the "GetById" queries
 */
public class QosQueryParameterBase extends IdQueryParameters {

    private static final long serialVersionUID = -4601447034328553847L;
    private QosType qosType;

    public QosQueryParameterBase() {
    }

    public QosQueryParameterBase(Guid dataCenterId, QosType qosType) {
        super(dataCenterId);
        this.qosType = qosType;
    }

    public QosQueryParameterBase(Guid dataCenterId) {
        super(dataCenterId);
    }

    public QosType getQosType() {
        return qosType;
    }

    public void setQosType(QosType qosType) {
        this.qosType = qosType;
    }

}
