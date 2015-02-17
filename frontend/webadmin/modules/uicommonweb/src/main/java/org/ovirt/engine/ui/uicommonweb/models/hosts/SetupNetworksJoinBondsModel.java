package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SetupNetworksJoinBondsModel extends SetupNetworksBondModel {

    private List<Entry<String, EntityModel<String>>> bondOptions = new ArrayList<Entry<String, EntityModel<String>>>();
    private Map<String, Entry<String, EntityModel<String>>> pairForBondOption = new HashMap<String, Entry<String, EntityModel<String>>>();

    @SuppressWarnings("unchecked")
    public SetupNetworksJoinBondsModel(List<String> freeBonds,
            BondNetworkInterfaceModel source,
            BondNetworkInterfaceModel target,
            Collection<String> suggestedLabels,
            Map<String, String> labelToIface) {

        setTitle(ConstantsManager.getInstance().getConstants().joinBondsTitle());

        Set<String> availableBonds = new HashSet<String>();
        availableBonds.addAll(freeBonds);
        availableBonds.add(source.getName());
        availableBonds.add(target.getName());
        getBond().setItems(availableBonds);
        getBond().setSelectedItem(target.getName());

        bondOptions.addAll(getBondingOptions().getItems());
        for (Entry<String, EntityModel<String>> pair : bondOptions) {
            pairForBondOption.put(getBondOptionForPair(pair), pair);
        }
        addBondOptionIfMissing(source.getBondOptions());
        addBondOptionIfMissing(target.getBondOptions());
        getBondingOptions().setItems(bondOptions);
        getBondingOptions().setSelectedItem(pairForBondOption.get(target.getBondOptions()));

        setLabelsModel(new NicLabelModel(Arrays.asList(source.getIface(), target.getIface()), suggestedLabels, labelToIface));
    }

    private void addBondOptionIfMissing(String candidateOption) {
        if (!pairForBondOption.containsKey(candidateOption)) {
            EntityModel<String> entityModel = new EntityModel<String>();
            entityModel.setEntity(candidateOption);
            Entry<String, EntityModel<String>> newPair = new KeyValuePairCompat<String, EntityModel<String>>("custom", entityModel); //$NON-NLS-1$
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
