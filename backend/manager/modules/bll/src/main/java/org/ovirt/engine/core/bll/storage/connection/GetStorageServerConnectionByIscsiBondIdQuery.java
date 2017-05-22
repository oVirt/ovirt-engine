package org.ovirt.engine.core.bll.storage.connection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public class GetStorageServerConnectionByIscsiBondIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    @Inject
    private IscsiBondDao iscsiBondDao;

    public GetStorageServerConnectionByIscsiBondIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<StorageServerConnections> conns = new ArrayList<>();

        for (String id : getConnectionsIds()) {
            conns.add(storageServerConnectionDao.get(id));
        }

        getQueryReturnValue().setReturnValue(conns);
    }

    private List<String> getConnectionsIds() {
        return iscsiBondDao.getStorageConnectionIdsByIscsiBondId(getParameters().getId());
    }
}
