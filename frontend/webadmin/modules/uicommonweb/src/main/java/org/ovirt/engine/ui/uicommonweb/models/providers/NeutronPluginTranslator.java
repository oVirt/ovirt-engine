package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkPluginType;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

public class NeutronPluginTranslator {

    private static final Map<String, OpenstackNetworkPluginType> pluginForDisplay;

    static {
        pluginForDisplay = new HashMap<String, OpenstackNetworkPluginType>();
        for (OpenstackNetworkPluginType plugin : OpenstackNetworkPluginType.values()) {
            pluginForDisplay.put(EnumTranslator.createAndTranslate(plugin), plugin);
        }
    }

    public static Set<String> getPresetDisplayStrings() {
        return pluginForDisplay.keySet();
    }

    public static boolean isDisplayStringCustom(String displayString) {
        return !pluginForDisplay.containsKey(displayString);
    }

    public static OpenstackNetworkPluginType getPluginTypeForDisplayString(String displayString) {
        return pluginForDisplay.get(displayString);
    }

    public static String getDisplayStringForPluginName(String pluginName) {
        try {
            return EnumTranslator.createAndTranslate(OpenstackNetworkPluginType.valueOf(pluginName));
        }
        catch (IllegalArgumentException e) {
            return pluginName;
        }
    }

    public static String getPluginNameForDisplayString(String displayString) {
        OpenstackNetworkPluginType pluginType = pluginForDisplay.get(displayString);
        return (pluginType == null) ? displayString : pluginType.name();
    }

}
