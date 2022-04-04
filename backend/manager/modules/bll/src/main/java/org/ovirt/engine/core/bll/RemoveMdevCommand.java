package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.MdevParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
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
        vmDeviceDao.remove(deviceId);
        setSucceeded(true);
    }

    @Override
    protected boolean shouldValidateSpecParams() {
        return false;
    }
}
