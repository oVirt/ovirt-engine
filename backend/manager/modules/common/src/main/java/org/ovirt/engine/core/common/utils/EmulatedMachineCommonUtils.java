package org.ovirt.engine.core.common.utils;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.ovirt.engine.core.common.businessentities.ChipsetType;

public class EmulatedMachineCommonUtils {

    public static Predicate<String> chipsetMatches(ChipsetType chipsetType) {
        return emulatedMachine -> {
            ChipsetType emChipsetType = ChipsetType.fromMachineType(emulatedMachine); // emChipsetType == null for non-x86
            return (chipsetType == ChipsetType.I440FX && emChipsetType == null) || chipsetType == emChipsetType;
        };
    }

    public static String getSupportedByChipset(ChipsetType chipsetType, Set<String> supported, List<String> available) {
        return available
                .stream()
                .filter(supported::contains)
                .filter(chipsetMatches(chipsetType))
                .findFirst().orElse(null);
    }

}
