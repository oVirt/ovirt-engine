package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.QosType;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.compat.Guid;

public class QosMapper {

    @Mapping(from = QosBase.class, to = Qos.class)
    public static Qos map(QosBase entity, Qos template) {
        Qos model = template != null ? template : new Qos();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());

        Guid storagePoolId = entity.getStoragePoolId();
        if (storagePoolId != null) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(storagePoolId.toString());
            model.setDataCenter(dataCenter);
        }

        model.setDescription(entity.getDescription());
        mapQosTypeToModel(entity, model);
        if (entity.getQosType() != null) {
            model.setType(QosTypeMapper.map(entity.getQosType(), null));
        }
        return model;
    }

    private static void mapQosTypeToModel(QosBase entity, Qos model) {
        switch (entity.getQosType()) {
        case STORAGE:
            mapStorageQosToModel(entity, model);
            break;
        case CPU:
            mapCpuQosToModel(entity, model);
            break;
        case NETWORK:
            mapNetworkQosToModel(entity, model);
            break;
        case HOSTNETWORK:
            mapHostNetworkQosToModel(entity, model);
            break;
        default:
            throw new IllegalArgumentException("Unsupported QoS type");
        }
    }

    private static void mapHostNetworkQosToModel(QosBase entity, Qos model) {
        HostNetworkQos hostNetworkQos = verifyAndCast(entity, HostNetworkQos.class);
        if (hostNetworkQos != null) {
            model.setOutboundAverageLinkshare(hostNetworkQos.getOutAverageLinkshare());
            model.setOutboundAverageUpperlimit(hostNetworkQos.getOutAverageUpperlimit());
            model.setOutboundAverageRealtime(hostNetworkQos.getOutAverageRealtime());
        }
    }

    private static void mapNetworkQosToModel(QosBase entity, Qos model) {
        NetworkQoS networkQos = verifyAndCast(entity, NetworkQoS.class);

        if (networkQos != null) {
            model.setInboundAverage(networkQos.getInboundAverage());
            model.setInboundPeak(networkQos.getInboundPeak());
            model.setInboundBurst(networkQos.getInboundBurst());
            model.setOutboundAverage(networkQos.getOutboundAverage());
            model.setOutboundPeak(networkQos.getOutboundPeak());
            model.setOutboundBurst(networkQos.getOutboundBurst());
        }
    }

    private static void mapCpuQosToModel(QosBase entity, Qos model) {
        CpuQos cpuQos = verifyAndCast(entity, CpuQos.class);
        if (cpuQos != null) {
            model.setCpuLimit(cpuQos.getCpuLimit());
        }
    }

    private static void mapStorageQosToModel(QosBase entity, Qos model) {
        StorageQos storageQos = verifyAndCast(entity, StorageQos.class);

        if (storageQos != null) {
            model.setMaxThroughput(storageQos.getMaxThroughput());
            model.setMaxReadThroughput(storageQos.getMaxReadThroughput());
            model.setMaxWriteThroughput(storageQos.getMaxWriteThroughput());
            model.setMaxIops(storageQos.getMaxIops());
            model.setMaxReadIops(storageQos.getMaxReadIops());
            model.setMaxWriteIops(storageQos.getMaxWriteIops());
        }
    }

    private static <T> T verifyAndCast(Object toCast, Class<T> castTo) {
        if (toCast == null) {
            return null;
        }

        if (castTo.isAssignableFrom(toCast.getClass())) {
            return castTo.cast(toCast);
        } else {
            throw new IllegalArgumentException("Cannot cast \"" +
                    toCast +
                    "\" to \"" +
                    castTo +
                    "\", however given object should be capable of that.");
        }
    }

    private static QosBase createNewQosEntityForQosType(QosType qosType) {
        switch (qosType) {
        case STORAGE:
            return new StorageQos();
        case CPU:
            return new CpuQos();
        case NETWORK:
            return new NetworkQoS();
        case HOSTNETWORK:
            return new HostNetworkQos();
        default:
            throw new IllegalArgumentException("Unsupported QoS type");
        }
    }

    @Mapping(from = Qos.class, to = QosBase.class)
    public static QosBase map(Qos model, QosBase template) {
        if (template == null) {
            template = createNewQosEntityForQosType(model.getType());
        }

        if (model.isSetId()) {
            template.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            template.setName(model.getName());
        }
        if (model.isSetDataCenter() && model.getDataCenter().isSetId()) {
            template.setStoragePoolId(GuidUtils.asGuid(model.getDataCenter()
                .getId()));
        }
        if (model.isSetDescription()) {
            template.setDescription(model.getDescription());
        }

        mapQosToEntity(model, template);

        return template;
    }

    private static void mapQosToEntity(Qos model, QosBase entity) {
        QosType qosType = model.getType();
        if (qosType == null && entity.getQosType() != null) {
            qosType = QosTypeMapper.map(entity.getQosType(), null);
        }
        if (qosType != null) {
            switch (qosType) {
            case STORAGE:
                mapStorageQosToEntity(model, (StorageQos) entity);
                break;
            case CPU:
                mapCpuQosToEntity(model, (CpuQos) entity);
                break;
            case NETWORK:
                mapNetworkQosToEntity(model, (NetworkQoS) entity);
                break;
            case HOSTNETWORK:
                mapHostNetworkQosToEntity(model, (HostNetworkQos) entity);
                break;
            default:
                break;
            }
        }
    }

    private static void mapHostNetworkQosToEntity(Qos model, HostNetworkQos entity) {
        if (model.isSetOutboundAverageLinkshare()) {
            entity.setOutAverageLinkshare(model.getOutboundAverageLinkshare());
        }

        if (model.isSetOutboundAverageUpperlimit()) {
            entity.setOutAverageUpperlimit(model.getOutboundAverageUpperlimit());
        }

        if (model.isSetOutboundAverageRealtime()) {
            entity.setOutAverageRealtime(model.getOutboundAverageRealtime());
        }
    }

    private static QosBase mapNetworkQosToEntity(Qos model, NetworkQoS entity) {
        if (model.isSetInboundAverage()) {
            entity.setInboundAverage(IntegerMapper.mapMinusOneToNull(model.getInboundAverage()));
        }
        if (model.isSetInboundPeak()) {
            entity.setInboundPeak(IntegerMapper.mapMinusOneToNull(model.getInboundPeak()));
        }
        if (model.isSetInboundBurst()) {
            entity.setInboundBurst(IntegerMapper.mapMinusOneToNull(model.getInboundBurst()));
        }
        if (model.isSetOutboundAverage()) {
            entity.setOutboundAverage(IntegerMapper.mapMinusOneToNull(model.getOutboundAverage()));
        }
        if (model.isSetOutboundPeak()) {
            entity.setOutboundPeak(IntegerMapper.mapMinusOneToNull(model.getOutboundPeak()));

        }
        if (model.isSetOutboundBurst()) {
            entity.setOutboundBurst(IntegerMapper.mapMinusOneToNull(model.getOutboundBurst()));
        }
        return entity;
    }

    private static QosBase mapCpuQosToEntity(Qos model, CpuQos entity) {
        if (model.isSetCpuLimit()) {
            entity.setCpuLimit(IntegerMapper.mapMinusOneToNull(model.getCpuLimit()));
        }
        return entity;
    }

    private static void mapStorageQosToEntity(Qos model, StorageQos entity) {
        if (model.isSetMaxThroughput()) {
            entity.setMaxThroughput(IntegerMapper.mapMinusOneToNull(model.getMaxThroughput()));
        }
        if (model.isSetMaxReadThroughput()) {
            entity.setMaxReadThroughput(IntegerMapper.mapMinusOneToNull(model.getMaxReadThroughput()));
        }
        if (model.isSetMaxWriteThroughput()) {
            entity.setMaxWriteThroughput(IntegerMapper.mapMinusOneToNull(model.getMaxWriteThroughput()));
        }
        if (model.isSetMaxIops()) {
            entity.setMaxIops(IntegerMapper.mapMinusOneToNull(model.getMaxIops()));
        }
        if (model.isSetMaxReadIops()) {
            entity.setMaxReadIops(IntegerMapper.mapMinusOneToNull(model.getMaxReadIops()));
        }
        if (model.isSetMaxWriteIops()) {
            entity.setMaxWriteIops(IntegerMapper.mapMinusOneToNull(model.getMaxWriteIops()));
        }
    }

}
