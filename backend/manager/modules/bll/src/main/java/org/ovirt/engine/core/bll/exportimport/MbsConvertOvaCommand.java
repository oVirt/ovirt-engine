package org.ovirt.engine.core.bll.exportimport;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class MbsConvertOvaCommand<T extends ConvertOvaParameters> extends ConvertOvaCommand<T> {

    public MbsConvertOvaCommand(Guid commandId) {
        super(commandId);
    }

    public MbsConvertOvaCommand(T parameters, CommandContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeVmCommand() {
        OvaImportManagedBlockSupport.applyPreAttachedManagedBlockDevices(
                getParameters().getDisks(),
                getParameters().getPreAttachedManagedBlockDevicesByDiskId());
        super.executeVmCommand();
    }
}
