package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

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
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        VdcQueryReturnValue res = runInternalQuery(VdcQueryType.GetGraphicsDevices, new IdQueryParameters(getParameters().getDev().getVmId()));
        if (res.getSucceeded()) {
            List<GraphicsDevice> devices = res.getReturnValue();
            for (GraphicsDevice device : devices) {
                if (device.getGraphicsType().equals(getParameters().getDev().getGraphicsType())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_ONLY_ONE_DEVICE_WITH_THIS_GRAPHICS_ALLOWED);
                }
            }

            if (devices.size() > 0) {
                // it means that with the one to be added it would be more than one, need to check if supported
                Cluster cluster = getCluster();
                if (cluster == null) {
                    // instance type - supported
                    return true;
                }

                if (!FeatureSupported.multipleGraphicsSupported(cluster.getCompatibilityVersion())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_ONLY_ONE_GRAPHICS_SUPPORTED_IN_THIS_CLUSTER_LEVEL);
                }
            }

            return true;
        }

        return false;
    }
}
