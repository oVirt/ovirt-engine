package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.resource.QosResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQosResource extends AbstractBackendSubResource<Qos, QosBase> implements QosResource {

    protected BackendQossResource parent;

    public BackendQossResource getParent() {
        return parent;
    }

    public BackendQosResource(String id, BackendQossResource parent) {
        super(id, Qos.class, QosBase.class);
        this.parent = parent;
    }

    @Override
    public Qos get() {
        IdQueryParameters params = new IdQueryParameters(guid);
        Qos qos = performGet(QueryType.GetQosById, params);
        return qos;
    }

    @Override
    public Qos update(final Qos incoming) {
        QueryIdResolver<Guid> entityResolver =
                new QueryIdResolver<>(QueryType.GetQosById, IdQueryParameters.class);
        final QosBase qosBase =
                getEntity(new QueryIdResolver<>(QueryType.GetQosById, IdQueryParameters.class), true);
        return performUpdate(incoming,
                entityResolver,
                updateActionTypeForQosType(qosBase.getQosType()),
                (model, entity) -> {
                    QosParametersBase<QosBase> parameters = new QosParametersBase<>();
                    parameters.setQosId(guid);
                    parameters.setQos(map(incoming, entity));
                    return parameters;
                });
    }

    private ActionType updateActionTypeForQosType(QosType qosType) {
        switch (qosType) {
        case STORAGE:
            return ActionType.UpdateStorageQos;
        case CPU:
            return ActionType.UpdateCpuQos;
        case NETWORK:
            return ActionType.UpdateNetworkQoS;
        case HOSTNETWORK:
            return ActionType.UpdateHostNetworkQos;
        default:
            throw new IllegalArgumentException("Unsupported QoS type \"" + qosType + "\"");
        }
    }

    protected class UpdateParametersProvider implements ParametersProvider<Qos, QosBase> {
        @Override
        public ActionParametersBase getParameters(Qos incoming,
                QosBase entity) {
            QosParametersBase<QosBase> parameters = new QosParametersBase<>();
            parameters.setQosId(guid);
            parameters.setQos(map(incoming, entity));
            return parameters;
        }
    }

    @Override
    public Response remove() {
        get();
        QosParametersBase<?> params = new QosParametersBase<>();
        params.setQosId(GuidUtils.asGuid(id));
        return performAction(ActionType.RemoveStorageQos, params);
    }
}
