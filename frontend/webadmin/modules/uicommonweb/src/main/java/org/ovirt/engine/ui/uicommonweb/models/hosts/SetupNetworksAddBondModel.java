package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.List;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SetupNetworksAddBondModel extends SetupNetworksBondModel {

    public SetupNetworksAddBondModel(List<String> freeBonds, String defaultBondName, boolean doesBondHaveVmNetworkAttached){
        super(doesBondHaveVmNetworkAttached);
        setTitle(ConstantsManager.getInstance().getConstants().createNewBondTitle());
        getBond().setItems(freeBonds);
        getBond().setSelectedItem(defaultBondName);
    }

}
