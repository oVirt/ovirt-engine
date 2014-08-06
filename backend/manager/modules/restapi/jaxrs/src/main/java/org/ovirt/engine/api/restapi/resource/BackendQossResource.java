package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.api.model.QoSs;
import org.ovirt.engine.api.resource.QoSsResource;
import org.ovirt.engine.api.resource.QosResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
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
        validateParameters(qos, "name");
        validateEnums(QoS.class, qos);
        QosParametersBase<QosBase> params = new QosParametersBase<QosBase>();
        QosBase entity = null;
        VdcActionType addVdcActionType = null;
        switch (QosType.valueOf(qos.getType().toUpperCase())) {
        case STORAGE:
            entity = new StorageQos();
            addVdcActionType = VdcActionType.AddStorageQos;
            break;
        case CPU:
            entity = new CpuQos();
            addVdcActionType = VdcActionType.AddCpuQos;
            break;
        default:
            break;
        }
        params.setQos(map(qos, entity));
        if (dataCenterId != null) {
            entity.setStoragePoolId(dataCenterId);
        }
        return performCreate(addVdcActionType,
                params,
                new QueryIdResolver<Guid>(VdcQueryType.GetQosById, IdQueryParameters.class));
    }

    @Override
    protected Response performRemove(String id) {
        QosParametersBase<?> params = new QosParametersBase<>();
        params.setQosId(GuidUtils.asGuid(id));
        return performAction(VdcActionType.RemoveStorageQos, params);
    }

    @Override
    @SingleEntityResource
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

    @Override
    protected QoS doPopulate(QoS model, QosBase entity) {
        return model;
    }
}
