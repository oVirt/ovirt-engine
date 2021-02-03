package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class CpuUtils {

    // see https://bugzilla.redhat.com/show_bug.cgi?id=1905158
    // using ArrayList to allow null values and avoid NullPointerException on contains(null)
    private static ArrayList<String> tsxRemovalAffectedInsecureCpuNames = new ArrayList<>(Arrays.asList(
            "Intel Skylake Client Family",
            "Intel Skylake Server Family",
            "Intel Cascadelake Server Family"));

    private static ArrayList<String> tsxRemovalAffectedSecureCpuNames = new ArrayList<>(Arrays.asList(
            "Secure Intel Skylake Client Family",
            "Secure Intel Skylake Server Family",
            "Secure Intel Cascadelake Server Family"));

    public static boolean isCpuInsecureAndAffectedByTsxRemoval(String cpuName) {
        return tsxRemovalAffectedInsecureCpuNames.contains(cpuName);
    }

    public static boolean isCpuSecureAndAffectedByTsxRemoval(String cpuName) {
        return tsxRemovalAffectedSecureCpuNames.contains(cpuName);
    }
}
