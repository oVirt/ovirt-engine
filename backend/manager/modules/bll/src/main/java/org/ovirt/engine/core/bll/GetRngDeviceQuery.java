package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetRngDeviceQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDeviceDao vmDeviceDao;

    public GetRngDeviceQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        final List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                getParameters().getId(),
                VmDeviceGeneralType.RNG,
                VmDeviceType.VIRTIO.getName(),
                getUserID(),
                getParameters().isFiltered());

        if (vmDevices != null && !vmDevices.isEmpty()) {
            VmDevice dev = vmDevices.get(0);
            setReturnValue(Collections.singletonList(new VmRngDevice(dev)));
        } else {
            setReturnValue(Collections.emptyList());
        }
    }

}
