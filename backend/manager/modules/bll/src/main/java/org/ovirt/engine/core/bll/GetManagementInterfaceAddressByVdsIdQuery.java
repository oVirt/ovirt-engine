package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;

public class GetManagementInterfaceAddressByVdsIdQuery<P extends VdsIdParametersBase> extends QueriesCommandBase<P> {

    public GetManagementInterfaceAddressByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsNetworkInterface nic =
                getDbFacade().getInterfaceDAO().getManagedInterfaceForVds(getParameters().getVdsId(),
                        getUserID(),
                        getParameters().isFiltered());

        getQueryReturnValue().setReturnValue(nic == null ? null : nic.getAddress());
    }

}
