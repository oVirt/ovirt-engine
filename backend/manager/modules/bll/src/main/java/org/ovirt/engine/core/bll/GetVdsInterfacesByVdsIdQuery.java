package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetVdsInterfacesByVdsIdQuery<P extends GetVdsByVdsIdParameters> extends QueriesCommandBase<P> {
    public GetVdsInterfacesByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final List<VdsNetworkInterface> list = getDbFacade().getInterfaceDAO()
                .getAllInterfacesForVds(getParameters().getVdsId(), getUserID(), getParameters().isFiltered());

        // 1. here we return all interfaces (eth0, eth1, eth2) - the first
        // condition
        // 2. we also return bonds that connected to network and has interfaces
        // - the second condition
        // i.e.
        // we have:
        // Network | Interface
        // -------------------
        // red-> |->eth0
        // |->eth1
        // | |->eth2
        // blue-> |->bond0->|->eth3
        // |->bond1
        //
        // we return: eth0, eth1, eth2, eth3, bond0
        // we don't return bond1 because he is not connected to network and has
        // no child interfaces

        List<VdsNetworkInterface> interfaces = LinqUtils.filter(list, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(final VdsNetworkInterface i) {
                return (i.getBonded() == null || (i.getBonded() != null && i.getBonded())
                        && LinqUtils.filter(list, new Predicate<VdsNetworkInterface>() {
                            @Override
                            public boolean eval(VdsNetworkInterface bond) {
                                return StringHelper.EqOp(bond.getBondName(), i.getName());
                            }
                        }).size() > 0);

            }
        });

        getQueryReturnValue().setReturnValue(interfaces);
    }
}
