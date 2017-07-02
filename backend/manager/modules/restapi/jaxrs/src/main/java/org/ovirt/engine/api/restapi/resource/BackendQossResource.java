package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.QosType;
import org.ovirt.engine.api.model.Qoss;
import org.ovirt.engine.api.resource.QosResource;
import org.ovirt.engine.api.resource.QossResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQossResource extends AbstractBackendCollectionResource<Qos, QosBase> implements QossResource {

    protected Guid dataCenterId;

    protected BackendQossResource(String datacenterId) {
        super(Qos.class, QosBase.class);
        this.dataCenterId = asGuid(datacenterId);
    }

    @Override
    public Qoss list() {
        return mapCollection(getBackendCollection(QueryType.GetAllQosByStoragePoolId,
                new QosQueryParameterBase(dataCenterId)));
    }

    @Override
    public Response add(Qos qos) {
        validateParameters(qos, "name", "type");
        QosParametersBase<QosBase> params = new QosParametersBase<>();
        org.ovirt.engine.api.model.QosType qosType = qos.getType();
        QosBase qosEntity = createNewQosEntityForQosType(qosType);

        params.setQos(map(qos, qosEntity));
        if (dataCenterId != null) {
            qosEntity.setStoragePoolId(dataCenterId);
        }
        return performCreate(addActionTypeForQosType(qosType),
                params,
                new QueryIdResolver<Guid>(QueryType.GetQosById, IdQueryParameters.class));
    }

    private QosBase createNewQosEntityForQosType(QosType qosType) {
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
            throw new IllegalArgumentException("Unsupported QoS type \"" + qosType + "\"");
        }
    }

    private ActionType addActionTypeForQosType(QosType qosType) {
        switch (qosType) {
        case STORAGE:
            return ActionType.AddStorageQos;
        case CPU:
            return ActionType.AddCpuQos;
        case NETWORK:
            return ActionType.AddNetworkQoS;
        case HOSTNETWORK:
            return ActionType.AddHostNetworkQos;
        default:
            throw new IllegalArgumentException("Unsupported QoS type \"" + qosType + "\"");

        }
    }

    @Override
    public QosResource getQosResource(String id) {
        return inject(new BackendQosResource(id, this));
    }

    protected Qoss mapCollection(List<QosBase> entities) {
        Qoss collection = new Qoss();
        for (QosBase entity : entities) {
            collection.getQoss().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected Qos addParents(Qos qos) {
        qos.setDataCenter(new DataCenter());
        qos.getDataCenter().setId(dataCenterId.toString());
        return qos;
    }
}
