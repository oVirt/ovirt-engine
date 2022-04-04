package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.MdevParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;

@ValidateSupportsTransaction
public class AddMdevCommand extends AbstractMdevCommand<MdevParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    public AddMdevCommand(MdevParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDevice mdev = getParameters().getDevice();
        if (mdev.getDeviceId() == null) {
            mdev.setDeviceId(Guid.newGuid());
        }
        vmDeviceDao.save(mdev);
        setSucceeded(true);
        setActionReturnValue(mdev.getId().getDeviceId());
    }

    @Override
    protected boolean shouldValidateDeviceId() {
        return false;
    }
}
