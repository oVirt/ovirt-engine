package org.ovirt.engine.core.common.vdscommands;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class CollectHostNetworkDataVdsCommandParameters extends VdsIdAndVdsVDSCommandParametersBase {

    private List<VdsNetworkInterface> nics;

    public CollectHostNetworkDataVdsCommandParameters() {
    }

    public CollectHostNetworkDataVdsCommandParameters(VDS vds) {
        super(vds);
        nics = Collections.emptyList();
    }

    public CollectHostNetworkDataVdsCommandParameters(VDS vds, List<VdsNetworkInterface> nics) {
        super(vds);
        this.nics = nics;
    }

    public List<VdsNetworkInterface> getInterfaces() {
        return nics;
    }
}
