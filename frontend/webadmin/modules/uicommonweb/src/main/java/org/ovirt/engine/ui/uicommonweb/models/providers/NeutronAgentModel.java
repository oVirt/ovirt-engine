package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;

public class NeutronAgentModel extends EntityModel {

    private ListModel<String> pluginType = new ListModel<>();

    private IValidation pluginValidator;

    public ListModel<String> getPluginType() {
        return pluginType;
    }

    public boolean validate() {
        if (getIsAvailable()) {
            getPluginType().validateSelectedItem(new IValidation[] { pluginValidator });
            setIsValid(getPluginType().getIsValid());
        }
        return getIsValid();
    }

    public void init(Provider<OpenstackNetworkProviderProperties> provider, ProviderType type) {
        OpenstackNetworkProviderProperties properties = provider.getAdditionalProperties();
        NetworkProviderPluginTranslator translator = NetworkProviderPluginTranslator.getTranslatorByProviderType(type);
        String pluginName = translator.getDisplayStringForPluginName(properties == null ?
            translator.getDefault() : properties.getPluginType());
        List<String> displayItems = translator.getPresetDisplayStrings();
        getPluginType().setItems(displayItems);
        getPluginType().setSelectedItem(pluginName);
        pluginValidator = translator.getPluginValidator();
    }

    public void flush(Provider<OpenstackNetworkProviderProperties> provider) {
        OpenstackNetworkProviderProperties properties = provider.getAdditionalProperties();
        if (properties == null) {
            properties = new OpenstackNetworkProviderProperties();
            provider.setAdditionalProperties(properties);
        }
        properties.setPluginType(NetworkProviderPluginTranslator.
                getPluginNameForDisplayString(getPluginType().getSelectedItem()));
    }
}
