package org.ovirt.engine.core.common.businessentities;

/**
 * Variables, that exists in {@code dwh_history_timekeeping} table
 */
public enum DwhHistoryTimekeepingVariable {
    LAST_SYNC("lastSync"),

    LAST_FULL_HOST_CHECK("lastFullHostCheck"),

    LAST_OS_INFO_UPDATE("lastOsinfoUpdate"),

    /**
     * This variable is used to tell DWH that engine is alive
     */
    HEART_BEAT("heartBeat"),

    /**
     * Nonexistent variable, used to fix {@code null} problems
     */
    UNDEFINED(null);

    /**
     * Name of the variable in {@code dwh_history_timekeeping} table
     */
    private String varName;

    private DwhHistoryTimekeepingVariable(String varName) {
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    /**
     * Creates an enum instance for specified variable name
     *
     * @param varName
     *            specified variable name
     * @return enum instance of {@code null} if specified variable is invalid
     */
    public static DwhHistoryTimekeepingVariable forVarName(String varName) {
        DwhHistoryTimekeepingVariable result = UNDEFINED;

        if (varName != null) {
            for (DwhHistoryTimekeepingVariable var : DwhHistoryTimekeepingVariable.values()) {
                if (var.getVarName().equals(varName)) {
                    result = var;
                    break;
                }
            }
        }
        return result;
    }
}
