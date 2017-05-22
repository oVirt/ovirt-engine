package org.ovirt.engine.core.bll.storage.connection.iscsibond;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.IscsiBondDao;

public class GetIscsiBondsByStoragePoolIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private IscsiBondDao iscsiBondDao;

    public GetIscsiBondsByStoragePoolIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<IscsiBond> iscsiBonds = iscsiBondDao.getAllByStoragePoolId(getParameters().getId());

        for (IscsiBond iscsiBond : iscsiBonds) {
            List<Guid> networkIds = iscsiBondDao.getNetworkIdsByIscsiBondId(iscsiBond.getId());
            iscsiBond.setNetworkIds(networkIds);

            List<String> connectionIds = iscsiBondDao.getStorageConnectionIdsByIscsiBondId(iscsiBond.getId());
            iscsiBond.setStorageConnectionIds(connectionIds);
        }

        getQueryReturnValue().setReturnValue(iscsiBonds);
    }
}
