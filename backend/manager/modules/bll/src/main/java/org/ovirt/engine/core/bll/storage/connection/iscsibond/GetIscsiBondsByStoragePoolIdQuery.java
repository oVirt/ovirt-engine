package org.ovirt.engine.core.bll.storage.connection.iscsibond;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetIscsiBondsByStoragePoolIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetIscsiBondsByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<IscsiBond> iscsiBonds = getDbFacade().getIscsiBondDao().getAllByStoragePoolId(getParameters().getId());

        for (IscsiBond iscsiBond : iscsiBonds) {
            List<Guid> networkIds = getDbFacade().getIscsiBondDao().getNetworkIdsByIscsiBondId(iscsiBond.getId());
            iscsiBond.setNetworkIds(networkIds);

            List<String> connectionIds = getDbFacade().getIscsiBondDao().getStorageConnectionIdsByIscsiBondId(iscsiBond.getId());
            iscsiBond.setStorageConnectionIds(connectionIds);
        }

        getQueryReturnValue().setReturnValue(iscsiBonds);
    }
}
