package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoesImageExistVDSCommand<P extends GetImageInfoVDSCommandParameters> extends GetImageInfoVDSCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(DoesImageExistVDSCommand.class);

    public DoesImageExistVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
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
            log.warn(
                    "executeIrsBrokerCommand: getImageInfo on '{}' threw an exception - assuming image doesn't exist: {}",
                    getParameters().getImageId(), ex.getMessage());
            log.debug("Exception", ex);
            setReturnValue(false);
            return;
        }

        setReturnValue(true);
    }
}
