package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.common.action.MdevParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class RemoveMdevCommand extends AbstractMdevCommand<MdevParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    public RemoveMdevCommand(MdevParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDeviceId deviceId = getParameters().getDevice().getId();
        CompensationUtils.removeEntity(deviceId, vmDeviceDao, getCompensationContextIfEnabledByCaller());
        compensationStateChanged();
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        VmDevice device = getParameters().getDevice();
        if (device.getDeviceId() == null || device.getVmId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_REMOVE_MDEV_INVALID_PARAMS);
        }
        return true;
    }

}
