package org.ovirt.engine.core.bll.network.dc;

import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetNetworkLabelsByDataCenterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkDao networkDao;

    @Inject
    private InterfaceDao interfaceDao;

    public GetNetworkLabelsByDataCenterIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Set<String> labels = networkDao.getAllNetworkLabelsForDataCenter(getParameters().getId());
        labels.addAll(interfaceDao.getAllNetworkLabelsForDataCenter(getParameters().getId()));
        getQueryReturnValue().setReturnValue(labels);
    }
}
