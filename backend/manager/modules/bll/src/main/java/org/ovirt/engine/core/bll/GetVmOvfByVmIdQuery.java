package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmOvfByVmIdParameters;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmOvfByVmIdQuery<P extends GetVmOvfByVmIdParameters> extends QueriesCommandBase<P> {

    public GetVmOvfByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = getVmDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        if (vm == null || vm.getDbGeneration() != getParameters().getRequiredGeneration()) {
            getQueryReturnValue().setSucceeded(false);
            return;
        }


        String ovfData = generateOvfConfig(vm);

        if (ovfData == null) {
            return;
        }

        getQueryReturnValue().setReturnValue(ovfData);
    }

    protected VmDao getVmDao() {
        return getDbFacade().getVmDao();
    }

    protected String generateOvfConfig(VM vm) {
        return new OvfHelper().generateOvfConfigurationForVm(vm);
    }
}
