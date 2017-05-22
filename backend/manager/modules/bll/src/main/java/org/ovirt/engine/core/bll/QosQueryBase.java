package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.qos.CpuQosDao;
import org.ovirt.engine.core.dao.qos.QosBaseDao;
import org.ovirt.engine.core.dao.qos.QosDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;

public abstract class QosQueryBase extends QueriesCommandBase<QosQueryParameterBase> {
    @Inject
    private QosBaseDao qosBaseDao;

    @Inject
    private StorageQosDao storageQosDao;

    @Inject
    private CpuQosDao cpuQosDao;

    @Inject
    private NetworkQoSDao networkQoSDao;

    @Inject
    private HostNetworkQosDao hostNetworkQosDao;

    public QosQueryBase(QosQueryParameterBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected QosDao<?> getQosDao() {
        QosType qosType = getParameters().getQosType();
        if (qosType == null) {
            return qosBaseDao;
        }
        switch (qosType) {
        case STORAGE:
            return storageQosDao;
        case CPU:
            return cpuQosDao;
        case NETWORK:
            return networkQoSDao;
        case HOSTNETWORK:
            return hostNetworkQosDao;
        default:
            log.debug("Not handled QoS type: '{}'", qosType);
            break;
        }
        return null;
    }

}
