package org.ovirt.engine.core.common.queries;

public class OsQueryParameters extends VdcQueryParametersBase {

    private OsRepositoryVerb osRepositoryVerb;
    private int osId;

    public OsQueryParameters() {
    }

    public OsQueryParameters(OsRepositoryVerb verb) {
        this.osRepositoryVerb = verb;
    }

    public OsQueryParameters(OsRepositoryVerb verb, int osId) {
        this.osRepositoryVerb = verb;
        setOsId(osId);
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

    public enum OsRepositoryVerb {
        HasSpiceSupport,
        GetLinuxOss,
        GetOsIds,
        GetMinimumOsRam,
        GetMaxOsRam,
        GetNetworkDevices,
        GetWindowsOss,
        GetUniqueOsNames,
        GetOsNames
    }
}
