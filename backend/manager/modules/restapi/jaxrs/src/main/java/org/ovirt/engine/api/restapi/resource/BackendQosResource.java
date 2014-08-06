package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.api.resource.QosResource;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQosResource extends AbstractBackendSubResource<QoS, QosBase> implements QosResource {

    protected BackendQossResource parent;

    public BackendQossResource getParent() {
        return parent;
    }

    public BackendQosResource(String id, BackendQossResource parent) {
        super(id, QoS.class, QosBase.class);
        this.parent = parent;
    }

    @Override
    public QoS get() {
        IdQueryParameters params = new IdQueryParameters(guid);
        QoS qos = performGet(VdcQueryType.GetQosById, params);
        return qos;
    }

    @Override
    public QoS update(final QoS incoming) {
        QueryIdResolver<Guid> entityResolver =
                new QueryIdResolver<Guid>(VdcQueryType.GetQosById, IdQueryParameters.class);
        final QosBase qosBase =
                getEntity(new QueryIdResolver<Guid>(VdcQueryType.GetQosById, IdQueryParameters.class), true);
        VdcActionType updateActionType = null;

        switch (qosBase.getQosType()) {
        case STORAGE:
            updateActionType = VdcActionType.UpdateStorageQos;
            break;
        case CPU:
            updateActionType = VdcActionType.UpdateCpuQos;
            break;

        default:
            break;
        }
        return performUpdate(incoming,
                entityResolver,
                updateActionType,
                new ParametersProvider<QoS, QosBase>() {
                    @Override
                    public VdcActionParametersBase getParameters(QoS model,
                            QosBase entity) {
                        QosParametersBase<QosBase> parameters = new QosParametersBase<QosBase>();
                        parameters.setQosId(guid);
                        parameters.setQos(map(incoming, entity));
                        return parameters;
                    }
                });
    }

    protected class UpdateParametersProvider implements ParametersProvider<QoS, QosBase> {
        @Override
        public VdcActionParametersBase getParameters(QoS incoming,
                QosBase entity) {
            QosParametersBase<QosBase> parameters = new QosParametersBase<QosBase>();
            parameters.setQosId(guid);
            parameters.setQos(map(incoming, entity));
            return parameters;
        }
    }

    @Override
    protected QoS doPopulate(QoS model, QosBase entity) {
        return model;
    }
}
