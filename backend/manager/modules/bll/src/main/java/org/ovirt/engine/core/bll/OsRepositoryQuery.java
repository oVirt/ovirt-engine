package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.OsQueryParameters;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;

public class OsRepositoryQuery<P extends OsQueryParameters> extends QueriesCommandBase<P> {

    private OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

    public OsRepositoryQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        switch (getParameters().getOsRepositoryVerb()) {
            case GetOsNames:
                setReturnValue(osRepository.getOsNames());
                break;
            case GetUniqueOsNames:
                setReturnValue(osRepository.getUniqueOsNames());
                break;
            case GetOsIds:
                setReturnValue(osRepository.getOsIds());
                break;
            case GetLinuxOss:
                setReturnValue(osRepository.getLinuxOss());
                break;
            case GetWindowsOss:
                setReturnValue(osRepository.getWindowsOss());
                break;
            case GetMaxOsRam:
                setReturnValue(osRepository.getMaximumRam(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetMinimumOsRam:
                setReturnValue(osRepository.getMinimumRam(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case HasSpiceSupport:
                setReturnValue(osRepository.hasSpiceSupport(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetNetworkDevices:
                setReturnValue(osRepository.getNetworkDevices(getParameters().getOsId(), getParameters().getVersion()));
                break;
        }
    }
}
