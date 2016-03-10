package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.InterfaceAndIdQueryParameters;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.utils.NetworkUtils;

/**
 * This query get vlan parent nic input: eth2.2 return: eth2
 */
public class GetVlanParentQuery<P extends InterfaceAndIdQueryParameters> extends QueriesCommandBase<P> {
    public GetVlanParentQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (NetworkCommonUtils.isVlan(getParameters().getInterface())) {
            List<VdsNetworkInterface> vdsInterfaces =
                    getDbFacade().getInterfaceDao().getAllInterfacesForVds(getParameters().getId());
            for (int i = 0; i < vdsInterfaces.size(); i++) {
                if (NetworkUtils.interfaceBasedOn(getParameters().getInterface(), vdsInterfaces.get(i).getName())) {
                    getQueryReturnValue().setReturnValue(vdsInterfaces.get(i));
                    break;
                }
            }
        }
    }
}
