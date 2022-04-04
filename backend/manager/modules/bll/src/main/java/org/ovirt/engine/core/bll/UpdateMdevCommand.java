package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.MdevParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class UpdateMdevCommand extends AbstractMdevCommand<MdevParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    public UpdateMdevCommand(MdevParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDevice mdev = getParameters().getDevice();
        vmDeviceDao.update(mdev);
        setSucceeded(true);
        setActionReturnValue(mdev.getId().getDeviceId());
    }
}
