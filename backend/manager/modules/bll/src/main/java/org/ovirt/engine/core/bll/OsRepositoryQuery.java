package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.OsQueryParameters;

public class OsRepositoryQuery<P extends OsQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private OsRepository osRepository;

    public OsRepositoryQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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
            case GetMinOsRam:
                setReturnValue(osRepository.getMinimumRam(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetDisplayTypes:
                setReturnValue(osRepository.getGraphicsAndDisplays());
                break;
            case HasNicHotplugSupport:
                setReturnValue(osRepository.hasNicHotplugSupport(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetFloppySupport:
                setReturnValue(osRepository.isFloppySupported(getParameters().getOsId(), getParameters().getVersion()));
                break;
            case GetDiskInterfaces:
                setReturnValue(osRepository.getDiskInterfaces(getParameters().getOsId(), getParameters().getVersion(),
                        getParameters().getChipset()));
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
            case Get64BitOss:
                setReturnValue(osRepository.get64bitOss());
                break;
            case GetVmInitMap:
                setReturnValue(osRepository.getVmInitMap());
                break;
            case GetTpmSupportMap:
                setReturnValue(osRepository.getTpmSupportMap());
                break;
            case GetUnsupportedOsIds:
                setReturnValue(osRepository.getUnsupportedOsIds());
                break;
            case GetMinCpus:
                setReturnValue(osRepository.getMinimumCpus(getParameters().getOsId()));
                break;
        }
    }
}
