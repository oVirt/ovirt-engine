package org.ovirt.engine.core.bll.storage.connection.iscsibond;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.IscsiBondDao;

public class GetIscsiBondByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetIscsiBondByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        IscsiBond iscsiBond = getIscsiBondDao().get(getParameters().getId());
        if (iscsiBond != null) {
            iscsiBond.getNetworkIds().addAll(getDbFacade().getIscsiBondDao()
                    .getNetworkIdsByIscsiBondId(iscsiBond.getId()));
            iscsiBond.getStorageConnectionIds().addAll(getDbFacade().getIscsiBondDao()
                    .getStorageConnectionIdsByIscsiBondId(iscsiBond.getId()));
            getQueryReturnValue().setReturnValue(iscsiBond);
        }
    }

    protected IscsiBondDao getIscsiBondDao() {
        return getDbFacade().getIscsiBondDao();
    }

}
