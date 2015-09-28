package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseDevice;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.ReadOnlyDeviceResource;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Guid;

public class BackendReadOnlyDeviceResource<D extends BaseDevice, C extends BaseResources, Q extends IVdcQueryable> extends AbstractBackendActionableResource<D, Q> implements ReadOnlyDeviceResource<D> {

    protected AbstractBackendReadOnlyDevicesResource<D, C, Q> collection;

    public BackendReadOnlyDeviceResource(Class<D> modelType,
                                         Class<Q> entityType,
                                         Guid guid,
                                         AbstractBackendReadOnlyDevicesResource<D, C, Q> collection,
                                         String... subCollections) {
        super(guid.toString(), modelType, entityType, subCollections);
        this.collection = collection;
    }

    @Override
    public D get() {
        Q entity = collection.lookupEntity(guid);
        if (entity == null) {
            return notFound();
        }
        return addLinks(populate(map(entity), entity));
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public D addParents(D device) {
        return collection.addParents(device);
    }

    AbstractBackendReadOnlyDevicesResource<D, C, Q> getCollection() {
        return collection;
    }
}
