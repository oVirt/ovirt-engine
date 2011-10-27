package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetVdsFreeBondsByVdsIdQuery<P extends GetVdsByVdsIdParameters> extends QueriesCommandBase<P> {
    public GetVdsFreeBondsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final List<VdsNetworkInterface> list = DbFacade.getInstance().getInterfaceDAO()
                .getAllInterfacesForVds(getParameters().getVdsId());

        // we return only bonds that are not active (that have no interfaces
        // related to them)
        // getQueryReturnValue().setReturnValue(null); //LINQ 31899
        // list.Where(bond =>
        // 31899 (bond.is_bond.HasValue && bond.is_bond.Value) &&
        // list.Where(iface => iface.bond_name == bond.name).ToList().Count ==
        // 0).ToList();

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
