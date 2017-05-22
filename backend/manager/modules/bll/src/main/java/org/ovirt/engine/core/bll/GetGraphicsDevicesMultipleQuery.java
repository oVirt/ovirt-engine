package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetGraphicsDevicesMultipleQuery<P extends IdsQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDeviceDao vmDeviceDao;

    public GetGraphicsDevicesMultipleQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        Map<Guid, List<GraphicsDevice>> result = new HashMap<>();

        mapDevices(result, VmDeviceType.SPICE, getParameters().getIds() );
        mapDevices(result, VmDeviceType.VNC, getParameters().getIds());

        setReturnValue(result);
    }

    private void mapDevices(Map<Guid, List<GraphicsDevice>> result, VmDeviceType type, List<Guid> vmsIds) {
        log.debug("Retrieving graphics devices '{}' for '{}' vms", type.getName(), vmsIds.size());
        List<VmDevice> devicesList = vmDeviceDao.getVmDeviceByTypeAndDevice(vmsIds,
                VmDeviceGeneralType.GRAPHICS,
                type.getName(),
                getUserID(),
                getParameters().isFiltered());

        for (VmDevice device : devicesList) {
            result.computeIfAbsent(device.getVmId(), guid -> new ArrayList<>()).add(new GraphicsDevice(device));
        }
    }
}
