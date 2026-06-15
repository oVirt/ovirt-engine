package org.ovirt.engine.core.bll.exportimport;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.vdscommands.ConvertOvaVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class ConvertOvaCommand<T extends ConvertOvaParameters> extends ConvertVmCommand<T> {

    public ConvertOvaCommand(Guid commandId) {
        super(commandId);
    }

    public ConvertOvaCommand(T parameters, CommandContext context) {
        super(parameters, context);
    }

    @Override
    protected VDSReturnValue runVdsCommand() {
        return runVdsCommand(
                VDSCommandType.ConvertOva,
                buildConvertParameters());
    }

    private ConvertOvaVDSParameters buildConvertParameters() {
        ConvertOvaVDSParameters parameters = new ConvertOvaVDSParameters(getVdsId());
        parameters.setOvaPath(getParameters().getOvaPath());
        parameters.setDisks(getParameters().getDisks());
        parameters.setVmId(getVmId());
        parameters.setVmName(getVmName());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setVirtioIsoPath(getVirtioIsoPath());
        return parameters;
    }
}
