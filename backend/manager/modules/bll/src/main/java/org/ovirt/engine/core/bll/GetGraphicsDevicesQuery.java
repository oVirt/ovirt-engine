package org.ovirt.engine.core.bll;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetGraphicsDevicesQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDeviceDao vmDeviceDao;

    public GetGraphicsDevicesQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        List<GraphicsDevice> result = new LinkedList<>();

        // we must use getVmDeviceByVmIdTypeAndDevice since it supports user filtering
        List<VmDevice> spiceDevs = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                getParameters().getId(),
                VmDeviceGeneralType.GRAPHICS,
                VmDeviceType.SPICE.getName(),
                getUserID(),
                getParameters().isFiltered());
        if (spiceDevs != null && !spiceDevs.isEmpty()) {
            result.add(new GraphicsDevice(spiceDevs.get(0)));
        }

        List<VmDevice> vncDevs = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                getParameters().getId(),
                VmDeviceGeneralType.GRAPHICS,
                VmDeviceType.VNC.getName(),
                getUserID(),
                getParameters().isFiltered());
        if (vncDevs != null && !vncDevs.isEmpty()) {
            result.add(new GraphicsDevice(vncDevs.get(0)));
        }

        setReturnValue(result);
    }

}
