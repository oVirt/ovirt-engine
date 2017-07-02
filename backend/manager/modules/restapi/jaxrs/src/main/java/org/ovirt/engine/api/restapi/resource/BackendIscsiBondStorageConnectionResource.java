package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;

public class BackendIscsiBondStorageConnectionResource extends BackendStorageServerConnectionResource {

    private BackendIscsiBondStorageConnectionsResource parent;

    public BackendIscsiBondStorageConnectionResource(String id, BackendIscsiBondStorageConnectionsResource parent) {
        super(id, parent);
        this.parent = parent;
    }

    @Override
    public StorageConnection get() {
        IscsiBond iscsiBond = parent.getIscsiBond();
        if (!iscsiBond.getStorageConnectionIds().contains(guid.toString())) {
            return notFound();
        }

        StorageServerConnections entity =
                getEntity(org.ovirt.engine.core.common.businessentities.StorageServerConnections.class,
                        QueryType.GetStorageServerConnectionById,
                        new StorageServerConnectionQueryParametersBase(guid),
                        guid.toString());

        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity));
    }

    @Override
    public Response remove() {
        get();
        IscsiBond iscsiBond = parent.getIscsiBond();
        iscsiBond.getStorageConnectionIds().remove(id);
        return performAction(ActionType.EditIscsiBond, new EditIscsiBondParameters(iscsiBond));
    }
}
