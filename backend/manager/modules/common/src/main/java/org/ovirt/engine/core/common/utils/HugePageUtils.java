package org.ovirt.engine.core.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.ovirt.engine.core.common.businessentities.HugePage;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;

public class HugePageUtils {
    private static final long KIB_IN_MIB = 1024;

    /**
     * Returns true iff there is a defined "hugepages" custom property with an integer value
     */
    public static boolean isBackedByHugepages(VmBase vm) {
        return getHugePageSize(vm).orElse(0) > 0;
    }

    /**
     * Returns a map of:
     * huge page size -> required amount of such huge pages
     */
    public static Map<Integer, Integer> getHugePages(VmBase vm) {
        Optional<Integer> hugePageSize = getHugePageSize(vm);
        if (!hugePageSize.isPresent()) {
            return Collections.emptyMap();
        }

        // Make sure we do not overflow when big memory VM is used
        int fullPages = (int)((KIB_IN_MIB * vm.getMemSizeMb() + hugePageSize.get() - 1) / hugePageSize.get());
        return Collections.singletonMap(hugePageSize.get(), fullPages);
    }

    /**
     * Get size of the hugepages in KiB.
     */
    public static Optional<Integer> getHugePageSize(VmBase vm) {
        SimpleCustomPropertiesUtil util = SimpleCustomPropertiesUtil.getInstance();
        Map<String, String> customProperties = util.convertProperties(vm.getCustomProperties());
        String hugePageStr = customProperties.get("hugepages");

        if (hugePageStr == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(hugePageStr));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
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

    public static Map<Integer, Integer> hugePagesToMap(List<HugePage> hugePages) {
        Map<Integer, Integer> hugePageMap = new HashMap<>(hugePages.size());
        for (HugePage hp: hugePages) {
            hugePageMap.put(hp.getSizeKB(), hp.getFree());
        }
        return hugePageMap;
    }

    public static int totalHugePageMemMb(Map<Integer, Integer> hugepages) {
        long hugePageMemKb = hugepages.entrySet().stream()
                .mapToLong(entry -> entry.getKey() * entry.getValue())
                .sum();

        return (int)((hugePageMemKb + KIB_IN_MIB - 1) / KIB_IN_MIB);
    }

    public static void updateHugePages(List<HugePage> hugePages, Integer sizeKb, Integer amount) {
        for (HugePage hugePage : hugePages) {
            if (Objects.equals(hugePage.getSizeKB(), sizeKb)) {
                hugePage.setFree(amount);
                break;
            }
        }
    }
}
