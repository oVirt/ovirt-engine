package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.osinfo.OsRepositoryImpl;
import org.ovirt.engine.core.common.queries.OsQueryParameters;

public class OsRepositoryQuery<P extends OsQueryParameters> extends QueriesCommandBase<P> {

    public OsRepositoryQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        switch (getParameters().getOsRepositoryVerb()) {
            case GetOsNames:
                setReturnValue(OsRepositoryImpl.INSTANCE.getOsNames());
                break;
            case GetUniqueOsNames:
                setReturnValue(OsRepositoryImpl.INSTANCE.getUniqueOsNames());
                break;
            case GetOsIds:
                setReturnValue(OsRepositoryImpl.INSTANCE.getOsIds());
                break;
            case GetLinuxOss:
                setReturnValue(OsRepositoryImpl.INSTANCE.getLinuxOss());
                break;
            case GetWindowsOss:
                setReturnValue(OsRepositoryImpl.INSTANCE.getWindowsOss());
                break;
            case GetMaxOsRam:
                setReturnValue(OsRepositoryImpl.INSTANCE.getMaximumRam(getParameters().getOsId(), null));
                break;
            case GetMinimumOsRam:
                setReturnValue(OsRepositoryImpl.INSTANCE.getMinimumRam(getParameters().getOsId(), null));
                break;
            case HasSpiceSupport:
                setReturnValue(OsRepositoryImpl.INSTANCE.hasSpiceSupport(getParameters().getOsId(), null));
                break;
            case GetNetworkDevices:
                setReturnValue(OsRepositoryImpl.INSTANCE.getNetworkDevices(getParameters().getOsId(), null));
                break;
        }
    }
}
