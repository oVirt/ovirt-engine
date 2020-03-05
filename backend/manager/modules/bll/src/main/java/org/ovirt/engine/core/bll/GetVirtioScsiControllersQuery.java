package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetVirtioScsiControllersQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    public GetVirtioScsiControllersQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid vmId = getParameters().getId() == null ? Guid.Empty : getParameters().getId();

        // vmId is empty GUID to get the virtio-SCSI interface for the pre created VM
        // if virtio-SCSI interface is disabled in pre VM creation, then attaching it will be blocked
        // by the backend validation

        getQueryReturnValue().setReturnValue(vmDeviceUtils.getVirtioScsiControllers(vmId,
                getUserID(), getParameters().isFiltered()));
    }
}
