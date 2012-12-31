package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetAllChildVlanInterfacesQueryParameters;
import org.ovirt.engine.core.utils.NetworkUtils;

/**
 * This query get interface and return all it's interface vlans, i.e input: eth2
 * return: eth2.4 eth2.5
 */
public class GetAllChildVlanInterfacesQuery<P extends GetAllChildVlanInterfacesQueryParameters>
        extends QueriesCommandBase<P> {
    public GetAllChildVlanInterfacesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<VdsNetworkInterface> retVal = new java.util.ArrayList<VdsNetworkInterface>();
        if (((VdsNetworkInterface) getParameters().getInterface()).getVlanId() == null) {
            List<VdsNetworkInterface> vdsInterfaces =
                    getDbFacade().getInterfaceDao().getAllInterfacesForVds(getParameters().getVdsId());
            for (int i = 0; i < vdsInterfaces.size(); i++) {
                if (vdsInterfaces.get(i).getVlanId() != null) {
                    if (NetworkUtils.interfaceBasedOn(vdsInterfaces.get(i).getName(),
                            getParameters().getInterface().getName())) {
                        retVal.add(vdsInterfaces.get(i));
                    }
                }
            }
        }
        getQueryReturnValue().setReturnValue(retVal);
    }
}
