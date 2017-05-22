package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class GetHostBondsByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private InterfaceDao interfaceDao;

    public GetHostBondsByHostIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VdsNetworkInterface> nics = interfaceDao.getAllInterfacesForVds(getParameters().getId());
        getQueryReturnValue().setReturnValue(NetworkCommonUtils.getBondsWithSlavesInformation(nics));
    }
}
