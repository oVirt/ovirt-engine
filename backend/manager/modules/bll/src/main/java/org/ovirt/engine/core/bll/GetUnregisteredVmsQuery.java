package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.domain.GetUnregisteredEntitiesQuery;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetUnregisteredVmsQuery<P extends IdQueryParameters> extends GetUnregisteredEntitiesQuery<P> {
    public GetUnregisteredVmsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<OvfEntityData> entityList = getOvfEntityList(VmEntityType.VM);
        List<VM> vmList = new ArrayList<>();
        for (OvfEntityData ovf : entityList) {
            try {
                VM vm = ovfHelper.readVmFromOvf(ovf.getOvfData()).getVm();

                // Setting the rest of the VM attributes which are not in the OVF.
                vm.setClusterCompatibilityVersion(ovf.getLowestCompVersion());
                vm.setClusterArch(ovf.getArchitecture());
                vm.setStatus(ovf.getStatus());
                vmList.add(vm);
            } catch (OvfReaderException e) {
                log.debug("failed to parse a given ovf configuration: \n" + ovf.getOvfData(), e);
                getQueryReturnValue().setExceptionString("failed to parse a given ovf configuration "
                        + e.getMessage());
            }
        }
        getQueryReturnValue().setSucceeded(true);
        getQueryReturnValue().setReturnValue(vmList);
    }
}
