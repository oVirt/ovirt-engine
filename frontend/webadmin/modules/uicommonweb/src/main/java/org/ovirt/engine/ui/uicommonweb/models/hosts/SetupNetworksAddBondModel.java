package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SetupNetworksAddBondModel extends SetupNetworksBondModel {

    public SetupNetworksAddBondModel(List<String> freeBonds,
            String defaultBondName,
            Collection<VdsNetworkInterface> ifaces,
            Collection<String> suggestedLabels,
            Map<String, String> labelToIface) {

        setTitle(ConstantsManager.getInstance().getConstants().createNewBondTitle());
        getBond().setItems(freeBonds);
        getBond().setSelectedItem(defaultBondName);

        setLabelsModel(new NicLabelModel(ifaces, suggestedLabels, labelToIface));
    }

}
