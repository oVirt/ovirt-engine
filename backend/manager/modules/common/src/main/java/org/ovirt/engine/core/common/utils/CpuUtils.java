package org.ovirt.engine.core.common.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.compat.Version;

public class CpuUtils {

    private static final String SECURE_PREFIX = "Secure ";

    private static final String LEGACY_SKYLAKE_CLIENT = "Intel Skylake Client IBRS SSBD MDS Family";

    private static final String LEGACY_SKYLAKE_SERVER = "Intel Skylake Server IBRS SSBD MDS Family";

    private static final String INSECURE_SKYLAKE_CLIENT = "Intel Skylake Client Family";

    private static final String SECURE_SKYLAKE_CLIENT = SECURE_PREFIX + INSECURE_SKYLAKE_CLIENT;

    private static final String INSECURE_SKYLAKE_SERVER = "Intel Skylake Server Family";

    private static final String SECURE_SKYLAKE_SERVER = SECURE_PREFIX + INSECURE_SKYLAKE_SERVER;

    private static final String INSECURE_CASCADELAKE_SERVER = "Intel Cascadelake Server Family";

    private static final String SECURE_CASCADELAKE_SERVER = SECURE_PREFIX + INSECURE_CASCADELAKE_SERVER;

    // see https://bugzilla.redhat.com/show_bug.cgi?id=1905158
    // using ArrayList to allow null values and avoid NullPointerException on contains(null)
    private static ArrayList<String> tsxRemovalAffectedInsecureCpuNames = new ArrayList<>(Arrays.asList(
            INSECURE_SKYLAKE_CLIENT,
            INSECURE_SKYLAKE_SERVER,
            INSECURE_CASCADELAKE_SERVER));

    private static ArrayList<String> tsxRemovalAffectedSecureCpuNames = new ArrayList<>(Arrays.asList(
            SECURE_SKYLAKE_CLIENT,
            SECURE_SKYLAKE_SERVER,
            SECURE_CASCADELAKE_SERVER));

    // see https://bugzilla.redhat.com/show_bug.cgi?id=1930733
    private static Map<String, String> secureToLegacyCpuNames = Stream.of(
            new SimpleEntry<String, String>(SECURE_SKYLAKE_CLIENT,
                    LEGACY_SKYLAKE_CLIENT),
            new SimpleEntry<String, String>(SECURE_SKYLAKE_SERVER,
                    LEGACY_SKYLAKE_SERVER))
            .collect(
                    Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    private static Map<String, String> legacyToSecureCpuNames = Stream.of(
            new SimpleEntry<String, String>(LEGACY_SKYLAKE_CLIENT,
                    SECURE_SKYLAKE_CLIENT),
            new SimpleEntry<String, String>(LEGACY_SKYLAKE_SERVER,
                    SECURE_SKYLAKE_SERVER))
            .collect(
                    Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));;

    public static boolean isCpuInsecureAndAffectedByTsxRemoval(String cpuName) {
        return tsxRemovalAffectedInsecureCpuNames.contains(cpuName);
    }

    public static boolean isCpuSecureAndAffectedByTsxRemoval(String cpuName) {
        return tsxRemovalAffectedSecureCpuNames.contains(cpuName);
    }

    public static String getCpuNameInVersion(ServerCpu oldCpu, Version newVersion) {
        if (oldCpu == null || newVersion == null) {
            return null;
        }

        if (Version.v4_4.lessOrEquals(newVersion)) {
            return legacyToSecureCpuNames.get(oldCpu.getCpuName());
        } else {
            return secureToLegacyCpuNames.get(oldCpu.getCpuName());
        }
    }
}
