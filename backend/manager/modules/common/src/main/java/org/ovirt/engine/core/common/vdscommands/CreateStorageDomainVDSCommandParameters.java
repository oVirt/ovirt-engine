package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateStorageDomainVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private StorageDomainStatic privateStorageDomain;

    public StorageDomainStatic getStorageDomain() {
        return privateStorageDomain;
    }

    private void setStorageDomain(StorageDomainStatic value) {
        privateStorageDomain = value;
    }

    private String privateArgs;

    public String getArgs() {
        return privateArgs;
    }

    private void setArgs(String value) {
        privateArgs = value;
    }

    public CreateStorageDomainVDSCommandParameters(Guid vdsId, StorageDomainStatic storageDomain, String args) {
        super(vdsId);
        setStorageDomain(storageDomain);
        setArgs(args);
    }

    public CreateStorageDomainVDSCommandParameters() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storageDomain", getStorageDomain())
                .append("args", getArgs());
    }
}
