package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetVdsFreeBondsByVdsIdQuery<P extends GetVdsByVdsIdParameters> extends QueriesCommandBase<P> {
    public GetVdsFreeBondsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final List<VdsNetworkInterface> list =
                getDbFacade().getInterfaceDao().getAllInterfacesForVds(getParameters().getVdsId());

        // we return only bonds that are not active (that have no interfaces
        // related to them)
        List<VdsNetworkInterface> interfaces = LinqUtils.filter(list, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(final VdsNetworkInterface bond) {
                return (bond.getBonded() != null && bond.getBonded())
                        && LinqUtils.filter(list, new Predicate<VdsNetworkInterface>() {
                            @Override
                            public boolean eval(VdsNetworkInterface iface) {
                                return iface.getBondName() != null && iface.getBondName().equals(bond.getName());
                            }
                        }).isEmpty();
            }
        });

        getQueryReturnValue().setReturnValue(interfaces);
    }
}
