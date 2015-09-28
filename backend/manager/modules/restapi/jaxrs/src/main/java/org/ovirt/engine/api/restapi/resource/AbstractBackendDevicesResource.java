package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseDevice;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendDevicesResource<D extends BaseDevice, C extends BaseResources, Q extends IVdcQueryable>
        extends AbstractBackendReadOnlyDevicesResource<D, C, Q>
        implements DevicesResource<D, C> {

    protected VdcActionType addAction;
    protected VdcActionType updateType;

    public AbstractBackendDevicesResource(Class<D> modelType,
                                          Class<C> collectionType,
                                          Class<Q> entityType,
                                          Guid parentId,
                                          VdcQueryType queryType,
                                          VdcQueryParametersBase queryParams,
                                          VdcActionType addAction,
                                          VdcActionType updateType,
                                          String... subCollections) {
        super(modelType, collectionType, entityType, parentId, queryType, queryParams, subCollections);
        this.addAction = addAction;
        this.updateType = updateType;
    }

    @Override
    public Response add(D device) {
        validateParameters(device, getRequiredAddFields());
        return performCreate(addAction,
                               getAddParameters(map(device), device),
                               getEntityIdResolver(device.getName()));
    }

    @Override
    public DeviceResource<D> getDeviceSubResource(String id) {
        return inject(new BackendDeviceResource<D, C, Q>(modelType,
                                                         entityType,
                                                         asGuidOr404(id),
                                                         this,
                                                         updateType,
                                                         getUpdateParametersProvider(),
                                                         getRequiredUpdateFields()));
    }

    public EntityIdResolver<Guid> getEntityIdResolver(String name) {
        return new DeviceIdResolver(name);
    }

    protected class DeviceIdResolver extends EntityIdResolver<Guid> {

        private String name;

        DeviceIdResolver(String name) {
            this.name = name;
        }

        private Q lookupEntity(Guid id, String name) {
            for (Q entity : getBackendCollection(queryType, queryParams)) {
                if (matchEntity(entity, id) || matchEntity(entity, name)) {
                    return entity;
                }
            }
            return null;
        }

        @Override
        public Q lookupEntity(Guid id) throws BackendFailureException {
            return lookupEntity(id, name);
        }
    }

    protected abstract boolean matchEntity(Q entity, String name);

    protected abstract String[] getRequiredAddFields();

    protected abstract String[] getRequiredUpdateFields();

    protected abstract VdcActionParametersBase getAddParameters(Q entity, D device);

    protected abstract ParametersProvider<D, Q> getUpdateParametersProvider();
}
