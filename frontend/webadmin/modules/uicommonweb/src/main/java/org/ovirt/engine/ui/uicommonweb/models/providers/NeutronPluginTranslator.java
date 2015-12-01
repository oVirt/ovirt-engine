package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkPluginType;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

public class NeutronPluginTranslator {

    private static final List<String> displayStrings;
    private static final Map<String, OpenstackNetworkPluginType> pluginForDisplay;

    static {
        pluginForDisplay = new HashMap<>();
        displayStrings = new ArrayList<>();
        for (OpenstackNetworkPluginType plugin : OpenstackNetworkPluginType.values()) {
            if (plugin != OpenstackNetworkPluginType.LINUX_BRIDGE) {
                String displayString = EnumTranslator.getInstance().translate(plugin);
                pluginForDisplay.put(displayString.toLowerCase(), plugin);
                displayStrings.add(displayString);
            }
        }
        Collections.sort(displayStrings, new LexoNumericComparator());
    }

    public static List<String> getPresetDisplayStrings() {
        return displayStrings;
    }

    public static boolean isDisplayStringCustom(String displayString) {
        return !pluginForDisplay.containsKey(displayString.toLowerCase());
    }

    public static OpenstackNetworkPluginType getPluginTypeForDisplayString(String displayString) {
        return pluginForDisplay.get(displayString.toLowerCase());
    }

    public static String getDisplayStringForPluginName(String pluginName) {
        try {
            return EnumTranslator.getInstance().translate(OpenstackNetworkPluginType.valueOf(pluginName));
        }
        catch (Exception e) {
            return pluginName == null ? "" : pluginName; //$NON-NLS-1$
        }
    }

    public static String getPluginNameForDisplayString(String displayString) {
        OpenstackNetworkPluginType pluginType = pluginForDisplay.get(displayString.toLowerCase());
        return (pluginType == null) ? displayString : pluginType.name();
    }

}
