package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.common.businessentities.ChipsetType;

public class ClusterEmulatedMachines {

    private static final String I440FX_CHIPSET_NAME = ChipsetType.I440FX.getChipsetName();
    private static final String Q35_CHIPSET_NAME = ChipsetType.Q35.getChipsetName();

    private String i440fxType = "";
    private String q35Type = "";

    private ClusterEmulatedMachines() {
    }

    private ClusterEmulatedMachines(String i440fxType, String q35Type) {
        this.i440fxType = i440fxType;
        this.q35Type = q35Type;
    }

    public static String build(String i440fxType, String q35Type) {
        if (i440fxType == null) {
            return q35Type;
        } else {
            return q35Type == null ? i440fxType : i440fxType + ";" + q35Type;
        }
    }

    public static boolean isMultiple(String emulatedMachine) {
        return emulatedMachine != null && emulatedMachine.contains(";");
    }

    public static ClusterEmulatedMachines parse(String emulatedMachine) {
        if (emulatedMachine == null) {
            return new ClusterEmulatedMachines();
        }
        String[] em = emulatedMachine.split(";");
        if (em.length == 0) {
            return new ClusterEmulatedMachines();
        }
        if (em.length == 2) {
            return new ClusterEmulatedMachines(em[0], em[1]);
        }
        ChipsetType chipsetType = ChipsetType.fromMachineType(emulatedMachine);
        if (chipsetType == ChipsetType.Q35) {
            return new ClusterEmulatedMachines(replaceChipset(emulatedMachine, ChipsetType.I440FX), emulatedMachine);
        } else {
            return new ClusterEmulatedMachines(emulatedMachine, replaceChipset(emulatedMachine, ChipsetType.Q35));
        }
    }

    protected static String replaceChipset(String emulatedMachine, ChipsetType chipsetType) {
        if (emulatedMachine == null || chipsetType == null) {
            return emulatedMachine;
        }
        switch (chipsetType) {
            case Q35:
                if (emulatedMachine.contains(I440FX_CHIPSET_NAME)) {
                    return emulatedMachine.replace(I440FX_CHIPSET_NAME, Q35_CHIPSET_NAME);
                }
                if (!emulatedMachine.contains(Q35_CHIPSET_NAME)) {
                    return emulatedMachine.replace("pc-", "pc-" + Q35_CHIPSET_NAME + '-');
                }
                break;

            case I440FX:
                if (emulatedMachine.contains(Q35_CHIPSET_NAME)) {
                    return emulatedMachine.replace(Q35_CHIPSET_NAME, I440FX_CHIPSET_NAME);
                }
                break;
        }
        return emulatedMachine;
    }

    public String getI440fxType() {
        return i440fxType;
    }

    public String getQ35Type() {
        return q35Type;
    }

    public String getTypeByChipset(ChipsetType chipsetType) {
        return chipsetType == ChipsetType.Q35 ? q35Type : i440fxType;
    }

    public static String forChipset(String emulatedMachine, ChipsetType chipsetType) {
        return ClusterEmulatedMachines.parse(emulatedMachine).getTypeByChipset(chipsetType);
    }

}
