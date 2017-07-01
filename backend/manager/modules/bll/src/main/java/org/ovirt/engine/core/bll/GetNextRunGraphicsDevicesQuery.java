package org.ovirt.engine.core.bll;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;

public class GetNextRunGraphicsDevicesQuery<P extends IdQueryParameters> extends GetGraphicsDevicesQuery<P> {

    public GetNextRunGraphicsDevicesQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        QueryReturnValue nextRun = runInternalQuery(QueryType.GetVmNextRunConfiguration, new IdQueryParameters(getParameters().getId()));
        VM vm = nextRun.getReturnValue();

        if (vm != null && vm.isNextRunConfigurationExists()) {
            List<GraphicsDevice> result = new LinkedList<>();
            for (GraphicsType graphicsType : GraphicsType.values()) {
                VmDevice device = VmDeviceCommonUtils.findVmDeviceByType(vm.getManagedVmDeviceMap(), graphicsType.getCorrespondingDeviceType());
                if (device != null) {
                    result.add(new GraphicsDevice(device));
                }
            }
            setReturnValue(result);
        } else {
            super.executeQueryCommand();
        }

    }
}
