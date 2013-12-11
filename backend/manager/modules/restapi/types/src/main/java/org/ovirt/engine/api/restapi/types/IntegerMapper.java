package org.ovirt.engine.api.restapi.types;

/**
 * Utility class containing mappings between various integer representations of special values
 * which differ between REST API and the backend.
 */
public final class IntegerMapper {

    /**
     * Facilitates conversion between integer that uses null to represent the default value on the backend
     * to integers that use -1 for default on the rest
     */
    public static Integer mapNullToMinusOne(Integer backendValue) {
        if (backendValue == null) {
            return -1;
        }

        return backendValue;
    }

    /**
     * Facilitates conversion between integer that uses -1 to represent default value on the rest side
     * to integers that use null for default on the backend
     */
    public static Integer mapMinusOneToNull(Integer restValue) {
        if (restValue == -1) {
            return null;
        }

        return restValue;
    }
}
