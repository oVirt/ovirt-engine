package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.QosType;

public class QosTypeMapper {

    @Mapping(from=org.ovirt.engine.core.common.businessentities.qos.QosType.class, to=QosType.class)
    public static QosType mapQosType(org.ovirt.engine.core.common.businessentities.qos.QosType qosType, QosType template) {
        switch (qosType) {

        case STORAGE: return QosType.STORAGE;
        case CPU: return QosType.CPU;
        case NETWORK: return QosType.NETWORK;
        default:
            throw new IllegalArgumentException(String.format("QosType %s cannot be mapped to %s: ", qosType, QosType.class));
        }
    }

    @Mapping(from=String.class, to=QosType.class)
    public static QosType mapQosType(String qosType, org.ovirt.engine.core.common.businessentities.qos.QosType template) {
        if (qosType == null) {
            if (template == null) {
                throw new IllegalStateException("Not specified which QosType should be used");
            } else {
                return QosTypeMapper.mapQosType(template, null);
            }
        } else {
            return mapModelQosType(qosType);
        }
    }

    private static QosType mapModelQosType(String type) {
        switch(type) {
            case "storage": return QosType.STORAGE;
            case "cpu": return QosType.CPU;
            case "network": return QosType.NETWORK;
            default:
                throw new IllegalArgumentException(
                    String.format("QosType %s cannot be mapped to %s: ", type, QosType.class));

        }
    }

    @Mapping(from=QosType.class, to=String.class)
    public static String qosTypeToString(org.ovirt.engine.core.common.businessentities.qos.QosType qosType) {
        return mapQosType(qosType, null).name().toLowerCase();
    }
}
