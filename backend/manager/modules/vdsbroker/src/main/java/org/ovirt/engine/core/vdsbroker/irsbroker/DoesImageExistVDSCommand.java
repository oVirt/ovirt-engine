package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class DoesImageExistVDSCommand<P extends GetImageInfoVDSCommandParameters> extends GetImageInfoVDSCommand<P> {
    public DoesImageExistVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        imageInfoReturn = getIrsProxy().getVolumeInfo(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(), getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString());

        try {
            proceedProxyReturnValue();
        }

        // NOTE: we should have been catching an IRSErrorImageNotExistException,
        // but since both the IRS doesn't send the correct status and we don't
        // check the correct status, we'll assume for now that any IRS exception
        // means that the image that we asked info about doesn't exist.
        catch (IRSErrorException ex) {
            log.warnFormat(
                    "IrsBrokerCommand::DoesImageExistVDSCommand::ExecuteIrsBrokerCommand: getImageInfo on {0} threw an exception - assuming image doesn't exist.",
                    getParameters().getImageId());
            setReturnValue(false);
            return;
        }

        setReturnValue(true);
    }

    private static Log log = LogFactory.getLog(DoesImageExistVDSCommand.class);
}
