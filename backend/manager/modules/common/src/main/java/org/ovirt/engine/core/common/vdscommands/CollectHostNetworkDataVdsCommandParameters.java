package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VDS;

public class CollectHostNetworkDataVdsCommandParameters extends VdsIdAndVdsVDSCommandParametersBase {

    public CollectHostNetworkDataVdsCommandParameters() {
    }

    public CollectHostNetworkDataVdsCommandParameters(VDS vds) {
        super(vds);
    }
}
