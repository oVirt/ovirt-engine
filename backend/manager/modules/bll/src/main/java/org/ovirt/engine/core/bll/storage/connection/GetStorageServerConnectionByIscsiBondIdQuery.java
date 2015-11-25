package org.ovirt.engine.core.bll.storage.connection;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetStorageServerConnectionByIscsiBondIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetStorageServerConnectionByIscsiBondIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<StorageServerConnections> conns = new ArrayList<>();

        for (String id : getConnectionsIds()) {
            conns.add(getDbFacade().getStorageServerConnectionDao().get(id));
        }

        getQueryReturnValue().setReturnValue(conns);
    }

    private List<String> getConnectionsIds() {
        return getDbFacade().getIscsiBondDao().getStorageConnectionIdsByIscsiBondId(getParameters().getId());
    }
}
