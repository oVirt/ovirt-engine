package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class GetVmDataByPoolIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetVmDataByPoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = DbFacade.getInstance()
                .getVmPoolDao()
                .getVmDataFromPoolByPoolGuid(getParameters().getId(), getUserID(), getParameters().isFiltered());

        if (vm != null) {
            VmHandler.updateVmInitFromDB(vm.getStaticData(), true);
        }

        getQueryReturnValue().setReturnValue(vm);


    }
}
