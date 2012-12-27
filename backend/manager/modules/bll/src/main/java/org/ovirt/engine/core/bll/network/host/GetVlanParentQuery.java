package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetAllChildVlanInterfacesQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;

/**
 * This query get vlan parent nic input: eth2.2 return: eth2
 */
public class GetVlanParentQuery<P extends GetAllChildVlanInterfacesQueryParameters> extends QueriesCommandBase<P> {
    public GetVlanParentQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (((VdsNetworkInterface) getParameters().getInterface()).getVlanId() != null) {
            List<VdsNetworkInterface> vdsInterfaces = DbFacade.getInstance()
                    .getInterfaceDao().getAllInterfacesForVds(getParameters().getVdsId());
            for (int i = 0; i < vdsInterfaces.size(); i++) {
                if (NetworkUtils.interfaceBasedOn(getParameters().getInterface().getName(),
                        vdsInterfaces.get(i).getName())) {
                    getQueryReturnValue().setReturnValue(vdsInterfaces.get(i));
                    break;
                }
            }
        }
    }
}
