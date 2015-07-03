package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public class AddGraphicsDeviceCommand extends AbstractGraphicsDeviceCommand<GraphicsParameters> {

    protected AddGraphicsDeviceCommand(GraphicsParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();
        if (graphicsDev.getDeviceId() == null) {
            graphicsDev.setDeviceId(Guid.newGuid());
        }
        getVmDeviceDao().save(graphicsDev);
        setSucceeded(true);
        setActionReturnValue(graphicsDev.getId().getDeviceId());
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        VdcQueryReturnValue res = runInternalQuery(VdcQueryType.GetGraphicsDevices, new IdQueryParameters(getParameters().getDev().getVmId()));
        if (res.getSucceeded()) {
            List<GraphicsDevice> devices = res.getReturnValue();
            for (GraphicsDevice device : devices) {
                if (device.getGraphicsType().equals(getParameters().getDev().getGraphicsType())) {
                    return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_ONLY_ONE_DEVICE_WITH_THIS_GRAPHICS_ALLOWED);
                }
            }

            return true;
        }

        return false;
    }
}
