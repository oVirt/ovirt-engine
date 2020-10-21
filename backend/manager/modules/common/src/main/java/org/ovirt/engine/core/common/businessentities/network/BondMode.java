package org.ovirt.engine.core.common.businessentities.network;

public enum BondMode {

    BOND0("0", "balance-rr", "(Mode 0) Round-robin", false),
    BOND1("1", "active-backup", "(Mode 1) Active-Backup", true),
    BOND2("2", "balance-xor", "(Mode 2) Load balance (balance-xor)", true),
    BOND3("3", "broadcast", "(Mode 3) Broadcast", true),
    BOND4("4", "802.3ad", "(Mode 4) Dynamic link aggregation (802.3ad)", true),
    BOND5("5", "balance-tlb", "(Mode 5) Adaptive transmit load balancing (balance-tlb)", false),
    BOND6("6", "balance-alb", "(Mode 6) Adaptive load balancing (balance-alb)", false);

    public static final String MODE = "mode=";
    private static final String DEFAULT_MIIMON_VALUE = "100";
    private static final String BOND_XMIT_POLICY_LAYER23 = "2";

    private String value;
    private String stringValue;
    private String description;
    private boolean isValidForVmNetwork;

    BondMode(String value, String stringValue, String description, boolean isValidForVmNetwork){
        this.value = value;
        this.stringValue = stringValue;
        this.description = description;
        this.isValidForVmNetwork = isValidForVmNetwork;
    }

    public String getValue(){
        return value;
    }

    public String getStringValue(){
        return stringValue;
    }

    public String getDescription(){
        return description;
    }

    public String getConfigurationValue(){
        return getConfigurationValue(DEFAULT_MIIMON_VALUE);
    }

    public String getConfigurationValue(String miimonValue){
        String extraOption;
        if (value.equals(BondMode.BOND4.value)) {
            extraOption = " xmit_hash_policy=" + BOND_XMIT_POLICY_LAYER23;
        } else {
            extraOption = " miimon=" + miimonValue;
        }
        return MODE + value + extraOption;
    }

    public boolean isBondModeValidForVmNetwork(){
        return isValidForVmNetwork;
    }

    public static BondMode parseBondMode(String bondOptions){
        return bondOptions == null ? null : getBondMode(findMode(bondOptions));
    }

    public static BondMode getBondMode(String bondModeValue){
        if (bondModeValue == null) {
            return null;
        }
        for (BondMode bondMode : BondMode.values()){
            if (bondMode.getStringValue().equals(bondModeValue) || bondMode.getValue().equals(bondModeValue)) {
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

        if (index==length || Character.isSpace(bondOptionsChars[index])) {
            return null;
        }

        int startIndex = index;
        while(index < length && !Character.isSpace(bondOptionsChars[index])){
            index++;
        }

        // GWT complains about Arrays.copyOfRange, using System.arraycopy instead
        char[] modeChars = new char[index - startIndex];
        System.arraycopy(bondOptionsChars, startIndex, modeChars, 0, index - startIndex);
        return new String(modeChars);
    }

    public static boolean isBondModeValidForVmNetwork(String bondOptions){
        BondMode bondMode = BondMode.parseBondMode(bondOptions);
        if (bondMode == null){
            return false;
        }
        return bondMode.isBondModeValidForVmNetwork();

    }
}
