package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.api.model.QoSs;
import org.ovirt.engine.api.model.QosType;
import org.ovirt.engine.api.resource.QoSsResource;
import org.ovirt.engine.api.resource.QosResource;
import org.ovirt.engine.api.restapi.types.QosTypeMapper;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQossResource extends AbstractBackendCollectionResource<QoS, QosBase> implements QoSsResource {

    protected Guid dataCenterId;

    protected BackendQossResource(String datacenterId) {
        super(QoS.class, QosBase.class);
        this.dataCenterId = asGuid(datacenterId);
    }

    @Override
    public QoSs list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllQosByStoragePoolId,
                new QosQueryParameterBase(dataCenterId)));
    }

    @Override
    public Response add(QoS qos) {
        validateParameters(qos, "name", "type");
        validateEnums(QoS.class, qos);
        QosParametersBase<QosBase> params = new QosParametersBase<>();
        org.ovirt.engine.api.model.QosType qosType = QosTypeMapper.mapQosType(qos.getType(), null);
        QosBase qosEntity = createNewQosEntityForQosType(qosType);

        params.setQos(map(qos, qosEntity));
        if (dataCenterId != null) {
            qosEntity.setStoragePoolId(dataCenterId);
        }
        return performCreate(addActionTypeForQosType(qosType),
                params,
                new QueryIdResolver<Guid>(VdcQueryType.GetQosById, IdQueryParameters.class));
    }

    private QosBase createNewQosEntityForQosType(QosType qosType) {
        switch (qosType) {
        case STORAGE:
            return new StorageQos();
        case CPU:
            return new CpuQos();
        case NETWORK:
            return new NetworkQoS();
        default:
            throw new IllegalArgumentException("Unsupported QoS type \"" + qosType + "\"");
        }
    }

    private VdcActionType addActionTypeForQosType(QosType qosType) {
        switch (qosType) {
        case STORAGE:
            return VdcActionType.AddStorageQos;
        case CPU:
            return VdcActionType.AddCpuQos;
        case NETWORK:
            return VdcActionType.AddNetworkQoS;
        default:
            throw new IllegalArgumentException("Unsupported QoS type \"" + qosType + "\"");

        }
    }

    @Override
    public QosResource getQosSubResource(String id) {
        return inject(new BackendQosResource(id, this));
    }

    protected QoSs mapCollection(List<QosBase> entities) {
        QoSs collection = new QoSs();
        for (QosBase entity : entities) {
            collection.getQoSs().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected QoS addParents(QoS qos) {
        qos.setDataCenter(new DataCenter());
        qos.getDataCenter().setId(dataCenterId.toString());
        return qos;
    }
}
