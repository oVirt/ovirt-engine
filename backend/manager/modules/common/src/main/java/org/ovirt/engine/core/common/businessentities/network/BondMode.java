package org.ovirt.engine.core.common.businessentities.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum BondMode {

    BOND0("0", "(Mode 0) Round-robin"),
    BOND1("1", "(Mode 1) Active-Backup"),
    BOND2("2", "(Mode 2) Load balance (balance-xor)"),
    BOND3("3", "(Mode 3) Broadcast"),
    BOND4("4", "(Mode 4) Dynamic link aggregation (802.3ad)"),
    BOND5("5", "(Mode 5) Adaptive transmit load balancing (balance-tlb)"),
    BOND6("6", "(Mode 6) Adaptive load balancing (balance-alb)");

    public static final Set<String> BOND_MODES_VALID_FOR_VM_NETWORK = new HashSet<>(Arrays.asList(BOND1.getValue(), BOND2.getValue(), BOND3.getValue(), BOND4.getValue()));
    public static final String MODE = "mode=";
    private static final String DEFAULT_MIIMON_VALUE = "100";

    private String value;
    private String description;

    BondMode(String value, String description){
        this.value = value;
        this.description = description;
    }

    public String getValue(){
        return value;
    }

    public String getDescription(){
        return description;
    }

    public String getConfigurationValue(){
        return getConfigurationValue(DEFAULT_MIIMON_VALUE);
    }

    public String getConfigurationValue(String miimonValue){
        return MODE + value + " miimon=" + miimonValue;
    }

    public static boolean isBondModeValidForVmNetwork(String bondMode){
        return BOND_MODES_VALID_FOR_VM_NETWORK.contains(bondMode);
    }

    public static String getBondMode(Bond bond){
        return getBondMode(bond.getBondOptions());
    }

    public static String getBondMode(String bondOptions){
        if(bondOptions == null){
            return BOND0.getValue();//no bond options, return the default bond option - 0
        }
        int modeStartIndex = bondOptions.indexOf(MODE);
        int bondModeIndex = modeStartIndex + MODE.length();
        if (modeStartIndex >= 0 && bondOptions.length() > bondModeIndex +1){
            return bondOptions.substring(bondModeIndex, bondModeIndex + 1);
        }
        return BOND0.getValue();
    }
}
