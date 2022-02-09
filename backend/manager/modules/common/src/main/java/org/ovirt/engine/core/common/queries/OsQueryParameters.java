package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.compat.Version;

public class OsQueryParameters extends QueryParametersBase {

    private OsRepositoryVerb osRepositoryVerb;
    private int osId;
    private Version version;
    private ChipsetType chipset;

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

    public OsQueryParameters(OsRepositoryVerb osRepositoryVerb, int osId, Version version, ChipsetType chipset) {
        this.osRepositoryVerb = osRepositoryVerb;
        this.osId = osId;
        this.version = version;
        this.chipset = chipset;
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

    public ChipsetType getChipset() {
        return chipset;
    }

    public void setChipset(ChipsetType chipset) {
        this.chipset = chipset;
    }

    public enum OsRepositoryVerb {
        GetDisplayTypes,
        HasNicHotplugSupport,
        GetNicHotplugSupportMap,
        GetDiskHotpluggableInterfacesMap,
        GetLinuxOss,
        GetOsIds,
        GetMinOsRam,
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
        GetSoundDeviceSupportMap,
        Get64BitOss,
        GetVmInitMap,
        GetTpmSupportMap,
        GetUnsupportedOsIds,
        GetMinCpus,
        GetTpmRequiredMap
    }
}
