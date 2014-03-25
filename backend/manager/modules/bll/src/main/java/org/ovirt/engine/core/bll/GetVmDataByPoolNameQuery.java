package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class GetVmDataByPoolNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    public GetVmDataByPoolNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = DbFacade.getInstance()
                .getVmPoolDao()
                .getVmDataFromPoolByPoolName(getParameters().getName(), getUserID(), getParameters().isFiltered());

        if (vm != null) {
            VmHandler.updateVmInitFromDB(vm.getStaticData(), true);
        }

        getQueryReturnValue().setReturnValue(vm);


    }
}
