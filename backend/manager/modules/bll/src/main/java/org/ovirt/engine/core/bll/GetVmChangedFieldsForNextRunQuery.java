package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.EditableDeviceOnVmStatusField;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.queries.GetVmChangedFieldsForNextRunParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;

import java.util.ArrayList;
import java.util.List;

public class GetVmChangedFieldsForNextRunQuery<P extends GetVmChangedFieldsForNextRunParameters>
        extends QueriesCommandBase<P>{

    public GetVmChangedFieldsForNextRunQuery(P parameters) {
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

        List<String> result = new ArrayList<>(VmHandler.getChangedFieldsForStatus(srcStatic, dstStatic, VMStatus.Up));

        for (Pair<EditableDeviceOnVmStatusField, Boolean> device :
                VmHandler.getVmDevicesFieldsToUpdateOnNextRun(srcVm.getId(), VMStatus.Up, getParameters().getUpdateVmParameters())) {
            if (device.getFirst().type() != VmDeviceType.UNKNOWN) {
                result.add(device.getFirst().type().getName());
            } else {
                result.add(device.getFirst().generalType().name());
            }
        }

        setReturnValue(result);
    }
}
