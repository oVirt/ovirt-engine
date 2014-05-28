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
            qosType = QosType.STORAGE;
        }
        switch (qosType) {
        case STORAGE:
            qosDao = getDbFacade().getStorageQosDao();
            break;

        default:
            break;
        }
        return qosDao;
    }

}
