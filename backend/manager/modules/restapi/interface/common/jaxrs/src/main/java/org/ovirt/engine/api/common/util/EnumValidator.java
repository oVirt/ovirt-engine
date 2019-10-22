/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.common.util;

import java.text.MessageFormat;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.OsTypeUtils;

public class EnumValidator {

    private static final String INVALID_ENUM_REASON = "Invalid value";
    private static final String INVALID_ENUM_DETAIL = "{0} is not a member of {1}";

    private static final Response.Status INVALID_ENUM_STATUS = Response.Status.BAD_REQUEST;

    /** Validate that @name is the name of an enum constant from the
     * enum class @clz.
     *
     * @param clz  the enum class
     * @param name the enum constant name; must not be null
     * @return     the enum constant
     * @throws WebApplicationException wrapping an appropriate response
     * iff the @name is invalid
     */
    public static <E extends Enum<E>> E validateEnum(Class<E> clz, String name) {
        return validateEnum(clz, name, false);
    }

    /**
     * Use this method wherever the list of possible values is extended beyond the enum values.;
     * This would commonly be seen in situations we where enums are deprecated they're value become system configurable;
     * instead of hard-coded. e.g {@link org.ovirt.engine.core.common.osinfo.OsRepository}
     * @param clz the emum class
     * @param externalValues the extended list of values to validate the input upon
     * @param name the actual value to be looked up. could be the enum member or part of extended list of values
     */
    public static <E extends Enum<E>> String validateEnum(Class<E> clz, Set<String> externalValues, String name, boolean toUppercase) {
        return validateEnum(INVALID_ENUM_REASON, INVALID_ENUM_DETAIL, clz, externalValues,  name, toUppercase);
    }


    public static <E extends Enum<E>> E validateEnum(Class<E> clz, String name, boolean toUppercase) {
        return validateEnum(INVALID_ENUM_REASON, INVALID_ENUM_DETAIL, clz, name, toUppercase);
    }

    /** Validate that @name is the name of an enum constant from the
     * enum class @clz.
     *
     * @param reason    the fault reason
     * @param detail    the fault detail
     * @param clz  the enum class
     * @param name the enum constant name; must not be null
     * @return     the enum constant
     * @throws WebApplicationException wrapping an appropriate response
     * iff the @name is invalid
     */
    public static <E extends Enum<E>> E validateEnum(String reason, String detail, Class<E> clz, String name) {
        return validateEnum(reason, detail, clz, name, false);
    }

    public static <E extends Enum<E>> E validateEnum(String reason, String detail, Class<E> clz, String name, boolean toUppercase) {
        try {
            return Enum.valueOf(clz, toUppercase ? name.toUpperCase() : name);
        } catch (IllegalArgumentException|NullPointerException e) {
            detail = detail + getPossibleValues(clz);
            throw new WebApplicationException(response(reason, MessageFormat.format(detail, name, clz.getSimpleName())));
        }
    }

    public static <E extends Enum<E>> String validateEnum(String reason, String detail, Class<E> clz, Set<String> externalValues, String name, boolean toUppercase) {
        for (String externalValue : externalValues) {
            if (externalValue.equalsIgnoreCase(name)) {
                return name;
            }
        }

        try {
            return Enum.valueOf(clz, toUppercase ? name.toUpperCase() : name).name();
        } catch (IllegalArgumentException|NullPointerException e) {
            detail = detail + getPossibleValues(clz, OsTypeUtils.getAllValues());
            throw new WebApplicationException(response(reason, MessageFormat.format(detail, name, clz.getSimpleName())));
        }
    }

    private static <E extends Enum<E>> String getPossibleValues(Class<E> clz, Set<String> allValues) {
        for (E enumValue: clz.getEnumConstants()) {
            allValues.add(enumValue.name().toLowerCase());
        }
        return ". Possible values for " + clz.getSimpleName() + " and its extended configurable values are: "
                + StringUtils.join(allValues.toArray(), ", ");
    }

    private static <E extends Enum<E>> String getPossibleValues(Class<E> clz) {
        StringBuilder builder = new StringBuilder(". Possible values for " + clz.getSimpleName() + " are:");
        for (E enumValue : clz.getEnumConstants()) {
            builder.append(" ").append(enumValue.name().toLowerCase()).append(",");
        }
        return builder.toString().substring(0, builder.toString().length() - 1);
    }

    private static Response response(String reason, String detail) {
        return Response.status(INVALID_ENUM_STATUS).entity(fault(reason, detail)).build();
    }

    private static Fault fault(String reason, String detail) {
        Fault fault = new Fault();
        fault.setReason(reason);
        fault.setDetail(detail);
        return fault;
    }
}
