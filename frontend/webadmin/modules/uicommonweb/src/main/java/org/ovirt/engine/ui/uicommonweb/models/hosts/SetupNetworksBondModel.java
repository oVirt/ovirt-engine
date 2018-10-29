package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.validation.CustomBondNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.KeyValueFormatValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NumberOnlyBondNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

@SuppressWarnings("unused")
public class SetupNetworksBondModel extends Model {

    public static final String CUSTOM_BONDING_MODE = "custom"; //$NON-NLS-1$

    private static RegexValidation CUSTOM_BOND_NAME_VALIDATION = new CustomBondNameValidation();
    private static RegexValidation NUM_ONLY_BOND_NAME_VALIDATION = new NumberOnlyBondNameValidation();

    private SortedListModel<String> privateBond;

    public SortedListModel<String> getBond() {
        return privateBond;
    }

    protected void setBond(SortedListModel<String> value) {
        privateBond = value;
    }

    private ListModel<Map.Entry<String, EntityModel<String>>> privateBondingOptions;

    public ListModel<Map.Entry<String, EntityModel<String>>> getBondingOptions() {
        return privateBondingOptions;
    }

    private void setBondingOptions(ListModel<Map.Entry<String, EntityModel<String>>> value) {
        privateBondingOptions = value;
    }

    private EntityModel<String> customBondEditor;

    public EntityModel<String> getCustomBondEditor() {
        return customBondEditor;
    }

    private void setCustomBondEditor(EntityModel<String> customBondEditor) {
        this.customBondEditor = customBondEditor;
    }

    public SetupNetworksBondModel() {
        this(false);
    }

    public SetupNetworksBondModel(boolean doesBondHaveVmNetworkAttached) {
        setBond(new SortedListModel<>(new LexoNumericComparator()));
        setBondingOptions(new ListModel<Map.Entry<String, EntityModel<String>>>());
        Map.Entry<String, EntityModel<String>> defaultItem = null;
        RefObject<Map.Entry<String, EntityModel<String>>> tempRef_defaultItem =
                new RefObject<>(defaultItem);
        ArrayList<Map.Entry<String, EntityModel<String>>> list =
                AsyncDataProvider.getInstance().getBondingOptionListDependingOnNetwork(tempRef_defaultItem, doesBondHaveVmNetworkAttached);
        defaultItem = tempRef_defaultItem.argvalue;
        getBondingOptions().setItems(list);
        getBondingOptions().setSelectedItem(defaultItem);
        setCustomBondEditor(new EntityModel<String>());
        getCustomBondEditor().getEntityChangedEvent().addListener((ev, sender, args) -> {
            final String customBondValue = ((EntityModel<String>) sender).getEntity();
            Map.Entry<String, EntityModel<String>> selectedItem = getBondingOptions().getSelectedItem();
            if (selectedItem.getKey().equals(CUSTOM_BONDING_MODE)) {
                selectedItem.getValue().setEntity(customBondValue);
            }
        });
        onBondingOptionsSelectionChange();
        getBondingOptions().getSelectedItemChangedEvent().addListener((ev, sender, args) -> onBondingOptionsSelectionChange());
    }

    public boolean validate(boolean customBondNameSupported) {
        getBond().validateSelectedItem(new IValidation[] { new NotEmptyValidation(),
                new LengthValidation(BusinessEntitiesDefinitions.HOST_NIC_NAME_LENGTH),
                customBondNameSupported ?
                        CUSTOM_BOND_NAME_VALIDATION :
                        NUM_ONLY_BOND_NAME_VALIDATION });
        getCustomBondEditor().setIsValid(true);
        if (getBondingOptions().getSelectedItem().getKey().equals(CUSTOM_BONDING_MODE)) {
            getCustomBondEditor().validateEntity(new IValidation[] { new KeyValueFormatValidation() });
        }

        return getBond().getIsValid() && getCustomBondEditor().getIsValid();
    }

    private void onBondingOptionsSelectionChange() {
        Map.Entry<String, EntityModel<String>> pair = getBondingOptions().getSelectedItem();
        if (CUSTOM_BONDING_MODE.equals(pair.getKey())) { //$NON-NLS-1$
            customBondEditor.setIsChangeable(true);
            String entity = pair.getValue().getEntity();
            customBondEditor.setEntity(entity == null ? "" : entity); //$NON-NLS-1$
        } else {
            customBondEditor.setIsChangeable(false);
        }
    }
}
