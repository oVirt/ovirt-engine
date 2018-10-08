package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ExternalNetworkPluginType;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkPluginType;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

public enum NetworkProviderPluginTranslator {

    NEUTRON(OpenstackNetworkPluginType.values(), OpenstackNetworkPluginType.OPEN_VSWITCH),
    EXTERNAL(ExternalNetworkPluginType.values(), null);

    private List<Enum> members = new ArrayList<>();
    private List<String> displayStrings = new ArrayList<>();
    private Enum defaultPlugin;

    private static final String EMPTY_NAME = "";
    private static final Map<String, Enum> pluginForDisplay  = new HashMap<>();
    private static final Map<String, Enum> pluginForName  = new HashMap<>();

    static {
        for (NetworkProviderPluginTranslator translator: NetworkProviderPluginTranslator.values()) {
            for (Enum plugin: translator.getMembers()) {
                String displayString = getDisplayString(plugin);
                translator.getPresetDisplayStrings().add(displayString);
                pluginForDisplay.put(displayString.toLowerCase(), plugin);
                pluginForName.put(plugin.name(), plugin);
            }
            Collections.sort(translator.getPresetDisplayStrings(), new LexoNumericComparator());

            String defaultDisplay = translator.defaultPlugin != null ?
                getDisplayString(translator.defaultPlugin) : EMPTY_NAME;
            if (!translator.getPresetDisplayStrings().contains(defaultDisplay)) {
                translator.getPresetDisplayStrings().add(0, defaultDisplay);
            }
        }
    }

    NetworkProviderPluginTranslator(Enum[] plugins, Enum defaultPlugin) {
        for(Enum plugin: plugins) {
            members.add(plugin);
        }
        this.defaultPlugin = defaultPlugin;
    }

    public List<String> getPresetDisplayStrings() {
        return displayStrings;
    }

    public String getDefault() {
        return defaultPlugin == null ? EMPTY_NAME : defaultPlugin.name();
    }

    private List<Enum> getMembers() {
        return members;
    }

    public String getDisplayStringForPluginName(String pluginName) {
        Enum plugin = pluginForName.get(pluginName);
        return plugin == null ? pluginName : getDisplayString(plugin);
    }

    public IValidation getPluginValidator() {
        if (this == EXTERNAL) {
            return value -> ValidationResult.ok();
        } else {
            return new NotEmptyValidation();
        }
    }

    public static String getPluginNameForDisplayString(String displayString) {
        Enum pluginType = pluginForDisplay.get(displayString.toLowerCase());
        return (pluginType == null) ? displayString : pluginType.name();
    }

    public static NetworkProviderPluginTranslator getTranslatorByProviderType(ProviderType providerType) {
        return providerType == ProviderType.EXTERNAL_NETWORK ?
            NetworkProviderPluginTranslator.EXTERNAL : NetworkProviderPluginTranslator.NEUTRON;
    }

    private static String getDisplayString(Enum plugin) {
        try {
            return EnumTranslator.getInstance().translate(plugin);
        } catch (Exception e) {
            return plugin.name();
        }
    }
}
