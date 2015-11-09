package org.ovirt.engine.api.model;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;

/**
 * This class is intended to hold the merged values of OsType and OsRepository
 */
public final class OsTypeUtils {

    private static Set<String> osTypeValues = new HashSet<>();

    static {
        // merge the backend list of oss with the OsType enum
        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        for (String uniqueName : osRepository.getUniqueOsNames().values()) {
            osTypeValues.add(uniqueName);
        }
        for (OsType type : OsType.values()) {
            osTypeValues.add(type.value());
        }
    }

    private OsTypeUtils() {
    }


    public static Set<String> getAllValues() {
        return osTypeValues;
    }
}
