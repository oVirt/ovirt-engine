package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SetupNetworksAddBondModel extends SetupNetworksBondModel {

    public SetupNetworksAddBondModel(List<VdsNetworkInterface> freeBonds) {
        setTitle(ConstantsManager.getInstance().getConstants().createNewBondTitle());
        setBootProtocol(NetworkBootProtocol.NONE);
        getBond().setItems(freeBonds);
    }

}
