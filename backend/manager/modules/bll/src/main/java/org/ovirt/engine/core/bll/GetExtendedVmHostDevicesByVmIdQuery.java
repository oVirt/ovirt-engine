package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.dao.HostDeviceDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Returns list of {@link HostDeviceView} entities representing configured host devices for specific VM.
 * Return list excludes those devices that are only placeholders for IOMMU group restriction.
 */
public class GetExtendedVmHostDevicesByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetExtendedVmHostDevicesByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Inject
    private HostDeviceDao hostDeviceDao;

    @Override
    protected void executeQueryCommand() {
        Set<String> primaryDevices = getNonPlaceholderDeviceNames();
        List<HostDeviceView> hostDeviceList = hostDeviceDao.getVmExtendedHostDevicesByVmId(getParameters().getId());

        List<HostDeviceView> result = new ArrayList<>();
        for (HostDeviceView deviceView : hostDeviceList) {
            if (primaryDevices.contains(deviceView.getDeviceName())) {
                result.add(deviceView);
            }
        }

        getQueryReturnValue().setReturnValue(result);
    }

    private Set<String> getNonPlaceholderDeviceNames() {
        List<VmHostDevice> vmHostDevices = runInternalQuery(VdcQueryType.GetVmHostDevices,
                new IdQueryParameters(getParameters().getId())).getReturnValue();

        Set<String> deviceNames = new HashSet<>();
        for (VmHostDevice device : vmHostDevices) {
            if (!device.isIommuPlaceholder()) {
                deviceNames.add(device.getDevice());
            }
        }

        return deviceNames;
    }
}
