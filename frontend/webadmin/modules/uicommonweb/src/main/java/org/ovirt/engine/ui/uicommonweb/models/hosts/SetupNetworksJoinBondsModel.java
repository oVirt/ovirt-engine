package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.BondMode;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SetupNetworksJoinBondsModel extends SetupNetworksBondModel {

    private List<Entry<String, EntityModel<String>>> bondOptions = new ArrayList<>();
    private Map<String, Entry<String, EntityModel<String>>> pairForBondOption = new HashMap<>();

    public SetupNetworksJoinBondsModel(List<String> freeBonds,
            BondNetworkInterfaceModel source,
            BondNetworkInterfaceModel target,
            boolean doesBondHaveVmNetworkAttached) {
        super(doesBondHaveVmNetworkAttached);

        setTitle(ConstantsManager.getInstance().getConstants().joinBondsTitle());

        Set<String> availableBonds = new HashSet<>();
        availableBonds.addAll(freeBonds);
        availableBonds.add(source.getName());
        availableBonds.add(target.getName());
        getBond().setItems(availableBonds);
        getBond().setSelectedItem(target.getName());

        bondOptions.addAll(getBondingOptions().getItems());
        for (Entry<String, EntityModel<String>> pair : bondOptions) {
            pairForBondOption.put(getBondOptionForPair(pair), pair);
        }

        addBondOptionIfMissing(source.getBondOptions(), doesBondHaveVmNetworkAttached);
        addBondOptionIfMissing(target.getBondOptions(), doesBondHaveVmNetworkAttached);
        getBondingOptions().setItems(bondOptions);
        getBondingOptions().setSelectedItem(pairForBondOption.get(target.getBondOptions()));
    }

    private void addBondOptionIfMissing(String candidateOption, boolean doesBondHaveVmNetworkAttached) {
        if (doesBondHaveVmNetworkAttached && !BondMode.isBondModeValidForVmNetwork(candidateOption)){
            return;
        }
        if (!pairForBondOption.containsKey(candidateOption)) {
            EntityModel<String> entityModel = new EntityModel<>();
            entityModel.setEntity(candidateOption);
            Entry<String, EntityModel<String>> newPair = new KeyValuePairCompat<>("custom", entityModel); //$NON-NLS-1$
            bondOptions.add(newPair);
            pairForBondOption.put(candidateOption, newPair);
        }
    }

    private String getBondOptionForPair(Entry<String, EntityModel<String>> pair) {
        String res = pair.getKey();
        if ("custom".equals(res)) { //$NON-NLS-1$
            return pair.getValue().getEntity();
        }
        return res;
    }

}
