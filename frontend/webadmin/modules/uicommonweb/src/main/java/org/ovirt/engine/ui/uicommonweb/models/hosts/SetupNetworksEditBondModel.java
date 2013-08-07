package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SetupNetworksEditBondModel extends SetupNetworksBondModel {

    public SetupNetworksEditBondModel(final VdsNetworkInterface bond) {
        setTitle(ConstantsManager.getInstance()
                .getMessages()
                .editBondInterfaceTitle(bond.getName()));

        // bond name
        getBond().setIsChangable(false);
        List<String> bondName = Arrays.asList(bond.getName());
        getBond().setItems(bondName);
        getBond().setSelectedItem(bond.getName());

        // bond options
        String bondOptions = bond.getBondOptions();
        List<KeyValuePairCompat<String, EntityModel>> items =
                (List<KeyValuePairCompat<String, EntityModel>>) getBondingOptions().getItems();
        boolean found = false;
        KeyValuePairCompat<String, EntityModel> customItem = null;
        for (KeyValuePairCompat<String, EntityModel> pair : items) {
            String key = pair.getKey();
            if (key.equals(bondOptions)) {
                getBondingOptions().setSelectedItem(pair);
                found = true;
                break;
            } else if ("custom".equals(key)) { //$NON-NLS-1$
                customItem = pair;
            }
        }
        if (!found) {
            EntityModel value = new EntityModel();
            value.setEntity(bondOptions);
            customItem.setValue(value);
            getBondingOptions().setSelectedItem(customItem);
        }
    }

}
