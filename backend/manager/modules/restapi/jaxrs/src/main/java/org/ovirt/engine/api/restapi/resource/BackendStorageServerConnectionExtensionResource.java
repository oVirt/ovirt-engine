package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnectionExtension;
import org.ovirt.engine.api.resource.StorageServerConnectionExtensionResource;
import org.ovirt.engine.api.restapi.types.StorageServerConnectionExtensionMapper;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.StorageServerConnectionExtensionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageServerConnectionExtensionResource
        extends AbstractBackendSubResource<StorageConnectionExtension, StorageServerConnectionExtension>
        implements StorageServerConnectionExtensionResource {
    private BackendStorageServerConnectionExtensionsResource parent;

    public BackendStorageServerConnectionExtensionResource(String id, BackendStorageServerConnectionExtensionsResource parent) {
        super(id, StorageConnectionExtension.class, StorageServerConnectionExtension.class);
        this.parent = parent;
    }

    @Override
    public StorageConnectionExtension get() {
        return performGet(VdcQueryType.GetStorageServerConnectionExtensionById, new IdQueryParameters(guid));
    }

    @Override
    public StorageConnectionExtension update(StorageConnectionExtension incoming) {
        QueryIdResolver resolver = new QueryIdResolver<>(VdcQueryType.GetStorageServerConnectionExtensionById,
                IdQueryParameters.class);
        return performUpdate(incoming, resolver, VdcActionType.UpdateStorageServerConnectionExtension , new UpdateParametersProvider());
    }

    public BackendStorageServerConnectionExtensionsResource getParent() {
        return parent;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveStorageServerConnectionExtension, new IdParameters(guid));
    }

    protected static class UpdateParametersProvider implements ParametersProvider<StorageConnectionExtension, StorageServerConnectionExtension> {
        @Override
        public VdcActionParametersBase getParameters(StorageConnectionExtension model, StorageServerConnectionExtension entity) {
            StorageServerConnectionExtension connExt = StorageServerConnectionExtensionMapper.map(model, entity);
            return new StorageServerConnectionExtensionParameters(connExt);
        }
    }
}
