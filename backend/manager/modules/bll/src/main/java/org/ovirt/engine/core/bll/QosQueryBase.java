package org.ovirt.engine.core.bll;


import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.dao.qos.QosDao;


public abstract class QosQueryBase extends QueriesCommandBase<QosQueryParameterBase> {
    private QosDao<?> qosDao;

    public QosQueryBase(QosQueryParameterBase parameters) {
        super(parameters);
    }

    protected QosDao<?> getQosDao() {
        QosType qosType = getParameters().getQosType();
        if (qosType == null) {
            return getDbFacade().getQosBaseDao();
        }
        switch (qosType) {
        case STORAGE:
            qosDao = getDbFacade().getStorageQosDao();
            break;
        case CPU:
            qosDao = getDbFacade().getCpuQosDao();
            break;
        case NETWORK:
            qosDao = getDbFacade().getNetworkQosDao();
            break;
        case HOSTNETWORK:
            qosDao = getDbFacade().getHostNetworkQosDao();
            break;
        default:
            log.debug("Not handled QoS type: '{}'", qosType);
            break;
        }
        return qosDao;
    }

}
