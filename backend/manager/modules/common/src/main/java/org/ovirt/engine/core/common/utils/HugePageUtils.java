package org.ovirt.engine.core.common.utils;

import java.util.Collections;
import java.util.Map;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;

@Singleton
public class HugePageUtils {

    /**
     * Returns true iff there is a defined "hugepages" custom property with an integer value
     */
    public boolean isBackedByHudepages(VmBase vm) {
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
    public Map<Integer, Integer> getHugePages(VmBase vm) {
        if (!isBackedByHudepages(vm)) {
            return Collections.EMPTY_MAP;
        }

        int hugePageSize = Integer.parseInt(getHugePageSize(vm));

        int fullPages = vm.getMemSizeMb() / hugePageSize;
        int lastPage = Math.min(1, vm.getMemSizeMb() % hugePageSize);
        return Collections.singletonMap(hugePageSize, fullPages + lastPage);
    }

    private String getHugePageSize(VmBase vm) {
        SimpleCustomPropertiesUtil util = SimpleCustomPropertiesUtil.getInstance();
        Map<String, String> customProperties = util.convertProperties(vm.getCustomProperties());
        return customProperties.get("hugepages");
    }
}
