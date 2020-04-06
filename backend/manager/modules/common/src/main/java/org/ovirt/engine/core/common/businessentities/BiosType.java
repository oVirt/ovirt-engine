package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BiosType implements Identifiable {

    /**
     * In VMs/templates this value means that the biosType of the cluster is to be used for the VM.
     *
     * In clusters this value means that the Engine should change biosType to the default value for the corresponding
     * architecture and compatibility version. This is done after the cluster's architecture is set or autodetected.
     */
    CLUSTER_DEFAULT(0, null, false),
    /**
     * For non-x86 architectures this value is the only value allowed
     */
    I440FX_SEA_BIOS(1, ChipsetType.I440FX, false),
    Q35_SEA_BIOS(2, ChipsetType.Q35, false),
    Q35_OVMF(3, ChipsetType.Q35, true),
    Q35_SECURE_BOOT(4, ChipsetType.Q35, true);

    private int value;
    private ChipsetType chipsetType;
    private boolean ovmf;

    private static final Map<Integer, BiosType> valueToBios =
            Stream.of(values()).collect(Collectors.toMap(BiosType::getValue, Function.identity()));

    BiosType(int value, ChipsetType chipsetType, boolean ovmf) {
        this.value = value;
        this.chipsetType = chipsetType;
        this.ovmf = ovmf;
    }

    public int getValue() {
        return value;
    }

    public ChipsetType getChipsetType() {
        return chipsetType;
    }

    public boolean isOvmf() {
        return ovmf;
    }

    public static BiosType forValue(int value) {
        return valueToBios.get(value);
    }

}
