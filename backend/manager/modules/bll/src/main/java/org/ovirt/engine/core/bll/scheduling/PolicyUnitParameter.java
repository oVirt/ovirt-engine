package org.ovirt.engine.core.bll.scheduling;

import java.util.regex.Pattern;

public enum PolicyUnitParameter {
    SCALE_DOWN("ScaleDown", "(100|[1-9]|[1-9][0-9])$"),

    /* The even vm count policy won't consider hosts with less than this amount
       of VM as migration sources, no matter how big the imbalance is. */
    HIGH_VM_COUNT("HighVmCount", "^([0-9]|[1-9][0-9]+)$"),

    /* The necessary imbalance in the amount of VM between two hosts needed
       for the automatic VM migration to kick in in even vm count distribution policy */
    MIGRATION_THRESHOLD("MigrationThreshold", "^([2-9]|[1-9][0-9]+)$"),

    /* A running SPM on a host is treated as the configured amount of VMs
       in the even vm count distribution policy. */
    SPM_VM_GRACE("SpmVmGrace", "^([0-9]|[1-9][0-9]+)$"),

    /* A host has to be over the high utilization for the defined time (in minutes) to
       be considered overutilized. */
    CPU_OVERCOMMIT_DURATION_MINUTES("CpuOverCommitDurationMinutes", "^([1-9][0-9]*)$"),

    /* The CPU load in percents over which a host is considered over utilized */
    HIGH_UTILIZATION("HighUtilization", "^([5-9][0-9])$"),

    /* The CPU load in percents under which a host is considered under utilized */
    LOW_UTILIZATION("LowUtilization", "^([0-9]|[1-4][0-9])$"),

    /* The ratio between the sum of VM's vCPUs to the physical host CPU cores count over which a host is considered over utilized */
    VCPU_TO_PHYSICAL_CPU_RATIO("VCpuToPhysicalCpuRatio", "^([0-2](\\.([0-9]+))?)$"),

    /* The amount of hosts that should be kept up although there are no VMs running
       Used in the power saving policy. */
    HOSTS_IN_RESERVE("HostsInReserve", "^[0-9][0-9]*$"),

    /* Enable / disable automatic host shutdown in power saving policy */
    ENABLE_AUTOMATIC_HOST_POWER_MANAGEMENT("EnableAutomaticHostPowerManagement", "^(true|false)$"),

    /**
     * The maximum amount of free memory that will still trigger the over utilization routines
     * (the host is over utilized when the available free memory amount is lower than the maximum limit)
     */
    LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED("MaxFreeMemoryForOverUtilized", "^([1-9][0-9]*)$"),

    /**
     * The minimum amount of free memory that will start triggering the under utilization routines
     * (the host is under utilized when the available free memory amount is greater than the minimal limit)
     */
    HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED("MinFreeMemoryForUnderUtilized", "^([1-9][0-9]*)$"),

    /**
     * The minimum amount of hosted engine capable hosts that have to be able to receive
     * the engine VM in terms of free memory.
     */
    HE_SPARES_COUNT("HeSparesCount", "^([0-9][0-9]*)$"),

    /**
     * The maximum allowed swap usage in percents of the physical memory
     */
    MAX_ALLOWED_SWAP_USAGE("MaximumAllowedSwapUsage", "^1?[0-9]?[0-9]$");

    final String dbName;
    final String regex;

    PolicyUnitParameter(String dbName, String regex) {
        this.dbName = dbName;
        this.regex = regex;
    }

    public String getDbName() {
        return dbName;
    }

    public String getRegex() {
        return regex;
    }

    public boolean validValue(String value) {
        Pattern pattern = Pattern.compile(getRegex());
        return pattern.matcher(value).matches();
    }
}
