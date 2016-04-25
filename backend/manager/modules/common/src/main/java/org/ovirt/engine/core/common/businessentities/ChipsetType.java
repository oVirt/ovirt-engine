package org.ovirt.engine.core.common.businessentities;

public enum ChipsetType {

    I440FX("i440fx"),
    Q35("q35");

    private String chipsetName;

    ChipsetType(String chipsetName) {
        this.chipsetName = chipsetName;
    }

    public String getChipsetName() {
        return chipsetName;
    }

    public static ChipsetType fromMachineType(String machineType) {
        ChipsetType defaultChipset = null;

        for (String element : machineType.split("-")) {
            for (ChipsetType chipsetType : values()) {
                if (element.equalsIgnoreCase(chipsetType.getChipsetName())) {
                    return chipsetType;
                }
                if (element.equalsIgnoreCase("pc")) {
                    defaultChipset = I440FX;
                }
            }
        }

        return defaultChipset;
    }

}
