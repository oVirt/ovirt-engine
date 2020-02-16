package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.common.businessentities.ChipsetType;

public class EmulatedMachineCommonUtils {

    private static final String I440FX_CHIPSET_NAME = ChipsetType.I440FX.getChipsetName();
    private static final String Q35_CHIPSET_NAME = ChipsetType.Q35.getChipsetName();

    public static String replaceChipset(String emulatedMachine, ChipsetType chipsetType) {
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

}
