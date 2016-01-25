package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.QosType;

public class QosTypeMapper {

    @Mapping(from=org.ovirt.engine.core.common.businessentities.qos.QosType.class, to=QosType.class)
    public static QosType map(org.ovirt.engine.core.common.businessentities.qos.QosType qosType, QosType template) {
        switch (qosType) {

        case STORAGE: return QosType.STORAGE;
        case CPU: return QosType.CPU;
        case NETWORK: return QosType.NETWORK;
        case HOSTNETWORK: return QosType.HOSTNETWORK;
        default:
            throw new IllegalArgumentException(String.format("QosType %s cannot be mapped to %s: ", qosType, QosType.class));
        }
    }
}
