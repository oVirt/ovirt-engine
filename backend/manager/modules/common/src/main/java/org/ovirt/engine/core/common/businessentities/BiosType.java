package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BiosType implements Identifiable {

    I440FX_SEA_BIOS(0, ChipsetType.I440FX, "i440fx/BIOS"),
    Q35_SEA_BIOS(1, ChipsetType.Q35, "q35/BIOS"),
    Q35_OVMF(2, ChipsetType.Q35, "q35/UEFI"),
    Q35_SECURE_BOOT(3, ChipsetType.Q35, "q35/SecureBoot");

    private int value;
    private ChipsetType chipsetType;
    private String description;

    private static final Map<Integer, BiosType> valueToBios =
            Stream.of(values()).collect(Collectors.toMap(BiosType::getValue, Function.identity()));

    BiosType(int value, ChipsetType chipsetType, String description) {
        this.value = value;
        this.chipsetType = chipsetType;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public ChipsetType getChipsetType() {
        return chipsetType;
    }

    public String getDescription() {
        return description;
    }

    public static BiosType forValue(int value) {
        return valueToBios.get(value);
    }

}
