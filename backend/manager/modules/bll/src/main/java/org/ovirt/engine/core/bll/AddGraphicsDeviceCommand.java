package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;

@ValidateSupportsTransaction
public class AddGraphicsDeviceCommand extends AbstractGraphicsDeviceCommand<GraphicsParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    private List<GraphicsDevice> prevDevices;

    public AddGraphicsDeviceCommand(GraphicsParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();
        if (graphicsDev.getDeviceId() == null) {
            graphicsDev.setDeviceId(Guid.newGuid());
        }

        CompensationUtils.saveEntity(graphicsDev, vmDeviceDao, getCompensationContextIfEnabledByCaller());
        compensationStateChanged();

        setSucceeded(true);
        setActionReturnValue(graphicsDev.getId().getDeviceId());
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        QueryReturnValue res = runInternalQuery(QueryType.GetGraphicsDevices,
                new IdQueryParameters(getVmBaseId()));
        if (res.getSucceeded()) {
            prevDevices = res.getReturnValue();
            for (GraphicsDevice device : prevDevices) {
                if (device.getGraphicsType().equals(getParameters().getDev().getGraphicsType())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_ONLY_ONE_DEVICE_WITH_THIS_GRAPHICS_ALLOWED);
                }
            }

            return true;
        }

        return false;
    }

    protected List<GraphicsDevice> getPrevDevices() {
        return prevDevices;
    }
}
