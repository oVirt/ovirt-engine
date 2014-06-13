package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.queries.GetVmUpdatesOnNextRunExistsParameters;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;

public class GetVmUpdatesOnNextRunExistsQuery<P extends GetVmUpdatesOnNextRunExistsParameters>
        extends QueriesCommandBase<P>{

    public GetVmUpdatesOnNextRunExistsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM srcVm = getParameters().getOriginal();
        VM dstVm = getParameters().getUpdated();
        VmStatic srcStatic = srcVm.getStaticData();
        VmStatic dstStatic = dstVm.getStaticData();

        // copy fields which are not saved as part of the OVF
        dstStatic.setExportDate(srcStatic.getExportDate());
        dstStatic.setManagedDeviceMap(srcStatic.getManagedDeviceMap());
        dstStatic.setUnmanagedDeviceList(srcStatic.getUnmanagedDeviceList());
        dstStatic.setOvfVersion(srcStatic.getOvfVersion());

        VmPropertiesUtils vmPropertiesUtils = SimpleDependecyInjector.getInstance().get(VmPropertiesUtils.class);

        vmPropertiesUtils.separateCustomPropertiesToUserAndPredefined(
                srcVm.getVdsGroupCompatibilityVersion(), srcStatic);
        vmPropertiesUtils.separateCustomPropertiesToUserAndPredefined(
                dstVm.getVdsGroupCompatibilityVersion(), dstStatic);

        setReturnValue(!VmHandler.isUpdateValid(srcStatic, dstStatic, VMStatus.Up) ||
                !VmHandler.isUpdateValidForVmDevices(srcVm.getId(), VMStatus.Up, getParameters().getUpdateVmParameters()));
    }
}
