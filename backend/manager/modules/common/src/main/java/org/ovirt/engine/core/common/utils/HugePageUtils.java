package org.ovirt.engine.core.common.utils;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;

public class HugePageUtils {
    private static final long KIB_IN_MIB = 1024;

    /**
     * Returns true iff there is a defined "hugepages" custom property with an integer value
     */
    public static boolean isBackedByHugepages(VmBase vm) {
        if (vm.getCustomProperties() == null || vm.getCustomProperties().isEmpty()) {
            return false;
        }

        String hugePage = getHugePageSize(vm);

        if (hugePage == null) {
            return false;
        }

        try {
            return Integer.parseInt(hugePage) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns a map of:
     * huge page size -> required amount of such huge pages
     */
    public static Map<Integer, Integer> getHugePages(VmBase vm) {
        if (!isBackedByHugepages(vm)) {
            return Collections.emptyMap();
        }

        int hugePageSize = Integer.parseInt(getHugePageSize(vm));

        // Make sure we do not overflow when big memory VM is used
        int fullPages = (int)((KIB_IN_MIB * vm.getMemSizeMb() + hugePageSize - 1) / hugePageSize);
        return Collections.singletonMap(hugePageSize, fullPages);
    }

    public static String getHugePageSize(VmBase vm) {
        SimpleCustomPropertiesUtil util = SimpleCustomPropertiesUtil.getInstance();
        Map<String, String> customProperties = util.convertProperties(vm.getCustomProperties());
        return customProperties.get("hugepages");
    }

    /**
     * A convenience method that makes it easier to express the difference
     * between normal and huge pages backed VM in scheduler.
     *
     * @return The amount of non huge page memory needed for the VM
     */
    public static Integer getRequiredMemoryWithoutHugePages(VmBase vmBase) {
        if (isBackedByHugepages(vmBase)) {
            return 0;
        } else {
            return vmBase.getMemSizeMb();
        }
    }

    public static boolean isHugepagesShared(VmBase vm) {
        SimpleCustomPropertiesUtil util = SimpleCustomPropertiesUtil.getInstance();
        Map<String, String> customProperties = util.convertProperties(vm.getCustomProperties());
        return Boolean.parseBoolean(customProperties.get("hugepages_shared"));
    }
}
