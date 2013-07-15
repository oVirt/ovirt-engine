package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;

public class CollectVdsNetworkDataAfterInstallationVDSCommand extends CollectVdsNetworkDataVDSCommand {

    public CollectVdsNetworkDataAfterInstallationVDSCommand(VdsIdAndVdsVDSCommandParametersBase parameters) {
        super(parameters);
    }

    /**
     * After installation, skip the management network since it is can be missing and we will add it afterwards.
     */
    @Override
    protected boolean skipManagementNetwork() {
        return true;
    }
}
