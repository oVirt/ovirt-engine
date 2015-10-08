package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Version;

public class OsQueryParameters extends VdcQueryParametersBase {

    private OsRepositoryVerb osRepositoryVerb;
    private int osId;
    private Version version;

    public OsQueryParameters() {
    }

    public OsQueryParameters(OsRepositoryVerb verb) {
        this.osRepositoryVerb = verb;
    }

    public OsQueryParameters(OsRepositoryVerb verb, int osId, Version version) {
        this.osRepositoryVerb = verb;
        this.version = version;
        this.osId = osId;
    }

    public int getOsId() {
        return osId;
    }

    public void setOsId(int osId) {
        this.osId = osId;
    }

    public OsRepositoryVerb getOsRepositoryVerb() {
        return osRepositoryVerb;
    }

    public Version getVersion() {
        return version;
    }

    public enum OsRepositoryVerb {
        GetDisplayTypes,
        GetBalloonSupportMap,
        IsBalloonEnabled,
        HasNicHotplugSupport,
        GetNicHotplugSupportMap,
        GetDiskHotpluggableInterfacesMap,
        GetLinuxOss,
        GetOsIds,
        GetMinimumOsRam,
        GetMaxOsRam,
        GetFloppySupport,
        GetDiskInterfaces,
        GetNetworkDevices,
        GetDiskHotpluggableInterfaces,
        GetVmWatchdogTypes,
        GetWindowsOss,
        GetUniqueOsNames,
        GetOsNames,
        GetOsArchitectures,
        GetDefaultOSes,
        GetSoundDeviceSupportMap
    }
}
