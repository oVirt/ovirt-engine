package org.ovirt.engine.core.common.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.compat.Version;

public class MDevTypesUtils {

    /**
     * Returns parsed mdev types from the custom properties (without the nodisplay)
     *
     * @param vm virtual machine
     * @return the list of mdev types (without the nodisplay configuration)
     */
    public static List<String> getMDevTypes(VM vm) {
        List<String> parsedMDevs = parseCustomProperties(vm);

        if (!isMdevDisplayOnSupported(vm.getCompatibilityVersion())) {
            return parsedMDevs;
        }

        if (containsNoDisplay(parsedMDevs)) {
            return parsedMDevs.subList(1, parsedMDevs.size());
        }
        return parsedMDevs;
    }

    /**
     * Checks if the mdev display on is supported and configured
     *
     * @param vm virtual machine
     * @return true if the mdev display is supported and configured
     */
    public static boolean isMdevDisplayOn(VM vm) {
        List<String> parsedMDevs = parseCustomProperties(vm);
        return !parsedMDevs.isEmpty() && isMdevDisplayOnSupported(vm.getCompatibilityVersion())
                && !containsNoDisplay(parsedMDevs);
    }

    private static boolean isMdevDisplayOnSupported(Version version) {
        return version.greaterOrEquals(Version.v4_3);
    }

    /**
     * Checks if the vm's mdev configuration in the custom properties contains
     * nodisplay as the first item
     *
     * @param vm virtual machine
     * @return true if the mdev types contain nodisplay configuration
     */
    private static boolean containsNoDisplay(List<String> parsedMDevs) {
        return parsedMDevs.size() > 0 && parsedMDevs.get(0).equals("nodisplay");
    }

    private static List<String> parseCustomProperties(VM vm) {
        SimpleCustomPropertiesUtil util = SimpleCustomPropertiesUtil.getInstance();
        Map<String, String> customProperties = util.convertProperties(vm.getCustomProperties());

        String mdevTypes = customProperties.get("mdev_type");
        if (mdevTypes == null || mdevTypes.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] mdevDevices = mdevTypes.split(",");
        return Arrays.asList(mdevDevices);
    }
}
