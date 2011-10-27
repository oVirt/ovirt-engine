package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.CreateStorageDomainVDSCommandParameters;

public class CreateStorageDomainVDSCommand<P extends CreateStorageDomainVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public CreateStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        if (!Config.<Boolean> GetValue(ConfigValues.SupportStorageFormat,
                getVds().getvds_group_compatibility_version()
                        .toString())) {
            status = getBroker().createStorageDomain(getParameters().getStorageDomain().getstorage_type().getValue(),
                    getParameters().getStorageDomain().getId().toString(),
                    getParameters().getStorageDomain().getstorage_name(), getParameters().getArgs(),
                    getParameters().getStorageDomain().getstorage_domain_type().getValue());
        } else {
            status =
                    getBroker().createStorageDomain(getParameters().getStorageDomain().getstorage_type().getValue(),
                            getParameters().getStorageDomain().getId().toString(),
                            getParameters().getStorageDomain().getstorage_name(),
                            getParameters().getArgs(),
                            getParameters().getStorageDomain().getstorage_domain_type().getValue(),
                            getParameters().getStorageDomain().getStorageFormat().getValue());
        }
        ProceedProxyReturnValue();
    }
}
