package org.ovirt.engine.core.common.businessentities.network;

public enum BondMode {

    BOND0("0", "(Mode 0) Round-robin", false),
    BOND1("1", "(Mode 1) Active-Backup", true),
    BOND2("2", "(Mode 2) Load balance (balance-xor)", true),
    BOND3("3", "(Mode 3) Broadcast", true),
    BOND4("4", "(Mode 4) Dynamic link aggregation (802.3ad)", true),
    BOND5("5", "(Mode 5) Adaptive transmit load balancing (balance-tlb)", false),
    BOND6("6", "(Mode 6) Adaptive load balancing (balance-alb)", false);

    public static final String MODE = "mode=";
    private static final String DEFAULT_MIIMON_VALUE = "100";

    private String value;
    private String description;
    private boolean isValidForVmNetwork;

    BondMode(String value, String description, boolean isValidForVmNetwork){
        this.value = value;
        this.description = description;
        this.isValidForVmNetwork = isValidForVmNetwork;
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

    public boolean isBondModeValidForVmNetwork(){
        return isValidForVmNetwork;
    }

    static BondMode getBondMode(String bondOptions){
        if(bondOptions == null){
            return null;
        }

        String bondModeValue = findMode(bondOptions);
        if (bondModeValue == null){
            return null;
        }

        for (BondMode bondMode : BondMode.values()){
            if (bondMode.getValue().equals(bondModeValue)){
                return bondMode;
            }
        }
        return null;
    }

    private static final String MODE_FOR_SEARCH = "mode";

    // Why we don't use regexp instead of this method?
    // This code is used both in the backend and in the UI. The UI is using GWT,
    // and does not support some features like regexp or the Character.isWhitespace
    // method. Using the ovirt compat package would also not work, as the regexp used
    // here would be a java regexp, and in the UI it would be used the javascript
    // regex engine.
    private static String findMode(String bondOptions){

        char[] bondOptionsChars = bondOptions.toCharArray();
        int length = bondOptions.length();

        // Find the start index for "mode"
        int index = 0;
        if (!bondOptions.startsWith(MODE_FOR_SEARCH)){
            if ((index = bondOptions.indexOf(" " + MODE_FOR_SEARCH)) == -1){
                return null;
            }
            index++; // compensate for the extra space in front of "mode"
        }

        index = index + MODE_FOR_SEARCH.length();

        // find "="
        if (index >= length || bondOptionsChars[index] != '='){
            return null;
        }
        index++;

        //find all the digits that make up the value
        StringBuilder bondModeBuilder = new StringBuilder();
        while(index < length && Character.isDigit(bondOptionsChars[index])){
            bondModeBuilder.append(bondOptionsChars[index]);
            index++;
        }

        // the digits must be followed by a space, if they are not the bond mode is not valid
        if (index < length && !Character.isSpace(bondOptionsChars[index])){
            return null;
        }

        if (bondModeBuilder.length() == 0){
            return null;
        }

        return bondModeBuilder.toString();
    }

    public static boolean isBondModeValidForVmNetwork(String bondOptions){
        BondMode bondMode = BondMode.getBondMode(bondOptions);
        if (bondMode == null){
            return false;
        }
        return bondMode.isBondModeValidForVmNetwork();

    }
}
