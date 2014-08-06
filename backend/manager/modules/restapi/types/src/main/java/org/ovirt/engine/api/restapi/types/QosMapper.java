package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.api.model.QosType;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;

public class QosMapper {

    @Mapping(from = QosBase.class, to = QoS.class)
    public static QoS map(QosBase entity, QoS template) {
        QoS model = template != null ? template : new QoS();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setType(org.ovirt.engine.api.model.QosType.fromValue(entity.getQosType().toString()).name().toLowerCase());
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(entity.getStoragePoolId().toString());
        model.setDescription(entity.getDescription());
        switch (entity.getQosType()) {
        case STORAGE:
            StorageQos storageQos = null;
            // avoid findbugs error.
            if (entity instanceof StorageQos) {
                storageQos = (StorageQos) entity;
            }
            // avoid findbugs error.
            if (storageQos == null) {
                return model;
            }
            model.setMaxThroughput(storageQos.getMaxThroughput());
            model.setMaxReadThroughput(storageQos.getMaxReadThroughput());
            model.setMaxWriteThroughput(storageQos.getMaxWriteThroughput());
            model.setMaxIops(storageQos.getMaxIops());
            model.setMaxReadIops(storageQos.getMaxReadIops());
            model.setMaxWriteIops(storageQos.getMaxWriteIops());
            break;
        case CPU:
            CpuQos cpuQos = null;
            // avoid findbugs error.
            if (entity instanceof CpuQos) {
                cpuQos = (CpuQos) entity;
            }
            // avoid findbugs error.
            if (cpuQos == null) {
                return model;
            }
            model.setCpuLimit(cpuQos.getCpuLimit());
            break;
        default:
            break;
        }

        return model;
    }

    @Mapping(from = QoS.class, to = QosBase.class)
    public static QosBase map(QoS model, QosBase template) {
        QosBase entity = null;
        if (template != null) {
            entity = template;
        }
        QosType qosType =
                model.getType() != null ? QosType.valueOf(model
                        .getType().toUpperCase()) : entity != null ? QosType.valueOf(entity
                        .getQosType().toString().toUpperCase()) : QosType.STORAGE;
        switch (qosType) {
        case STORAGE:
            if (entity == null) {
                entity = new StorageQos();
            }
            if (model.isSetMaxThroughput()) {
                ((StorageQos) entity)
                        .setMaxThroughput(IntegerMapper.mapMinusOneToNull(model.getMaxThroughput()));
            }
            if (model.isSetMaxReadThroughput()) {
                ((StorageQos) entity).setMaxReadThroughput(IntegerMapper.mapMinusOneToNull(model
                        .getMaxReadThroughput()));
            }
            if (model.isSetMaxWriteThroughput()) {
                ((StorageQos) entity).setMaxWriteThroughput(IntegerMapper.mapMinusOneToNull(model
                        .getMaxWriteThroughput()));
            }
            if (model.isSetMaxIops()) {
                ((StorageQos) entity)
                        .setMaxIops(IntegerMapper.mapMinusOneToNull(model.getMaxIops()));
            }
            if (model.isSetMaxReadIops()) {
                ((StorageQos) entity)
                        .setMaxReadIops(IntegerMapper.mapMinusOneToNull(model.getMaxReadIops()));
            }
            if (model.isSetMaxWriteIops()) {
                ((StorageQos) entity)
                        .setMaxWriteIops(IntegerMapper.mapMinusOneToNull(model.getMaxWriteIops()));
            }
            break;
        case CPU:
            if (entity == null) {
                entity = new CpuQos();
            }
            if (model.isSetCpuLimit()) {
                ((CpuQos) entity)
                        .setCpuLimit(IntegerMapper.mapMinusOneToNull(model.getCpuLimit()));
            }
            break;
        default:
            break;
        }

        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDataCenter() && model.getDataCenter().isSetId()) {
            entity.setStoragePoolId(GuidUtils.asGuid(model.getDataCenter()
                    .getId()));
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }

        return entity;
    }
}
