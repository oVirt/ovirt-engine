package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseDevice;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Guid;

public class BackendDeviceResource<D extends BaseDevice, C extends BaseResources, Q extends IVdcQueryable> extends BackendReadOnlyDeviceResource<D, C, Q> implements DeviceResource<D> {

    protected VdcActionType updateType;
    protected ParametersProvider<D, Q> updateParametersProvider;
    protected EntityIdResolver<Guid> entityResolver;
    protected String[] requiredUpdateFields;

    public BackendDeviceResource(Class<D> modelType,
            Class<Q> entityType,
            final Guid guid,
            final AbstractBackendReadOnlyDevicesResource<D, C, Q> collection,
            VdcActionType updateType,
            ParametersProvider<D, Q> updateParametersProvider,
            String[] requiredUpdateFields,
            String... subCollections) {
        super(modelType, entityType, guid, collection, subCollections);
        this.updateType = updateType;
        this.updateParametersProvider = updateParametersProvider;
        this.requiredUpdateFields = requiredUpdateFields;
        entityResolver = new EntityIdResolver<Guid>() {
            @Override
            public Q lookupEntity(Guid id) throws BackendFailureException {
                return collection.lookupEntity(guid);
            }
        };
    }

    @Override
    public D update(D resource) {
        validateParameters(resource, requiredUpdateFields);
        return performUpdate(resource, entityResolver, updateType, updateParametersProvider);
    }
}
