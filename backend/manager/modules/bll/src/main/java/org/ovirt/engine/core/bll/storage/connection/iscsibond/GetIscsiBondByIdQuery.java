package org.ovirt.engine.core.bll.storage.connection.iscsibond;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.IscsiBondDao;

public class GetIscsiBondByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private IscsiBondDao iscsiBondDao;

    public GetIscsiBondByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        IscsiBond iscsiBond = iscsiBondDao.get(getParameters().getId());
        if (iscsiBond != null) {
            iscsiBond.getNetworkIds().addAll(iscsiBondDao.getNetworkIdsByIscsiBondId(iscsiBond.getId()));
            iscsiBond.getStorageConnectionIds().addAll(
                    iscsiBondDao.getStorageConnectionIdsByIscsiBondId(iscsiBond.getId()));
            getQueryReturnValue().setReturnValue(iscsiBond);
        }
    }
}
