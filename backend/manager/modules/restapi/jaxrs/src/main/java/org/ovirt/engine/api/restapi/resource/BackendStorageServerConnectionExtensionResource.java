package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnectionExtension;
import org.ovirt.engine.api.resource.StorageServerConnectionExtensionResource;
import org.ovirt.engine.api.restapi.types.StorageServerConnectionExtensionMapper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.StorageServerConnectionExtensionParameters;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

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
        return performGet(QueryType.GetStorageServerConnectionExtensionById, new IdQueryParameters(guid));
    }

    @Override
    public StorageConnectionExtension update(StorageConnectionExtension incoming) {
        QueryIdResolver resolver = new QueryIdResolver<>(QueryType.GetStorageServerConnectionExtensionById,
                IdQueryParameters.class);
        return performUpdate(incoming, resolver, ActionType.UpdateStorageServerConnectionExtension , new UpdateParametersProvider());
    }

    public BackendStorageServerConnectionExtensionsResource getParent() {
        return parent;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveStorageServerConnectionExtension, new IdParameters(guid));
    }

    protected static class UpdateParametersProvider implements ParametersProvider<StorageConnectionExtension, StorageServerConnectionExtension> {
        @Override
        public ActionParametersBase getParameters(StorageConnectionExtension model, StorageServerConnectionExtension entity) {
            StorageServerConnectionExtension connExt = StorageServerConnectionExtensionMapper.map(model, entity);
            return new StorageServerConnectionExtensionParameters(connExt);
        }
    }
}
