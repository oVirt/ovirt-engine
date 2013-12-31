package org.ovirt.engine.core.bll.network.dc;

import java.util.Set;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNetworkLabelsByDataCenterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetNetworkLabelsByDataCenterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Set<String> labels = getDbFacade().getNetworkDao().getAllNetworkLabelsForDataCenter(getParameters().getId());
        labels.addAll(getDbFacade().getInterfaceDao().getAllNetworkLabelsForDataCenter(getParameters().getId()));
        getQueryReturnValue().setReturnValue(labels);
    }
}
