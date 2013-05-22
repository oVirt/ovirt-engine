package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VDS;

public class CollectVdsNetworkDataVDSCommandParameters extends VdsIdAndVdsVDSCommandParametersBase {

    private boolean skipMgmtNetwork;

    public CollectVdsNetworkDataVDSCommandParameters(VDS host) {
        super(host);
    }

    public CollectVdsNetworkDataVDSCommandParameters(VDS host, boolean skipMgmtNetwork) {
        super(host);
        this.skipMgmtNetwork = skipMgmtNetwork;
    }

    @Override
    public String toString() {
        return String.format("%s, skipMgmtNetwork=%s", super.toString(), isManagementNetworkSkipped());
    }

    public boolean isManagementNetworkSkipped() {
        return skipMgmtNetwork;
    }
}
