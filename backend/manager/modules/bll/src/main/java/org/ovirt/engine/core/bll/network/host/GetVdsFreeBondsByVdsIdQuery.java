package org.ovirt.engine.core.bll.network.host;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVdsFreeBondsByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsFreeBondsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final List<VdsNetworkInterface> list =
                getDbFacade().getInterfaceDao().getAllInterfacesForVds(getParameters().getId());

        // we return only bonds that are not active (that have no interfaces
        // related to them)
        List<VdsNetworkInterface> interfaces = list.stream().filter(bond ->
                (bond.getBonded() != null && bond.getBonded())
                && list.stream().noneMatch(iface -> iface.getBondName() != null && iface.getBondName().equals(bond.getName()))
        ).collect(Collectors.toList());

        getQueryReturnValue().setReturnValue(interfaces);
    }
}
