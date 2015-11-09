package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.OsQueryParameters;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;

public class OsRepositoryQuery<P extends OsQueryParameters> extends QueriesCommandBase<P> {

    private OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);

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
            case GetNicHotplugSupportMap:
                setReturnValue(osRepository.getNicHotplugSupportMap());
                break;
            case GetDiskHotpluggableInterfacesMap:
                setReturnValue(osRepository.getDiskHotpluggableInterfacesMap());
                break;
            case GetOsArchitectures:
                setReturnValue(osRepository.getOsArchitectures());
                break;
            case GetMaxOsRam:
                setReturnValue(osRepository.getMaximumRam(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetMinimumOsRam:
                setReturnValue(osRepository.getMinimumRam(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetDisplayTypes:
                setReturnValue(osRepository.getGraphicsAndDisplays());
                break;
            case GetBalloonSupportMap:
                setReturnValue(osRepository.getBalloonSupportMap());
            break;
            case IsBalloonEnabled:
                setReturnValue(osRepository.isBalloonEnabled(getParameters().getOsId(), getParameters().getVersion()));
            break;
            case HasNicHotplugSupport:
                setReturnValue(osRepository.hasNicHotplugSupport(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetFloppySupport:
                setReturnValue(osRepository.isFloppySupported(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetDiskInterfaces:
                setReturnValue(osRepository.getDiskInterfaces(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetNetworkDevices:
                setReturnValue(osRepository.getNetworkDevices(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetDiskHotpluggableInterfaces:
                setReturnValue(osRepository.getDiskHotpluggableInterfaces(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetVmWatchdogTypes:
                setReturnValue(osRepository.getVmWatchdogTypes(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetDefaultOSes:
                setReturnValue(osRepository.getDefaultOSes());
                break;
            case GetSoundDeviceSupportMap:
                setReturnValue(osRepository.getSoundDeviceSupportMap());
                break;
        }
    }
}
