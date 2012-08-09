/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

import java.text.MessageFormat;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Fault;

public class EnumValidator {

    private static final String INVALID_ENUM_REASON = "Invalid value";
    private static final String INVALID_ENUM_DETAIL = "{0} is not a member of {1}";

    private static final Response.Status INVALID_ENUM_STATUS = Response.Status.BAD_REQUEST;

    /* Validate that @name is the name of an enum constant from the
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

    public static <E extends Enum<E>> E validateEnum(Class<E> clz, String name, boolean toUppercase) {
        return validateEnum(INVALID_ENUM_REASON, INVALID_ENUM_DETAIL, clz, name, toUppercase);
    }

    /* Validate that @name is the name of an enum constant from the
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
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(response(reason, MessageFormat.format(detail, name, clz.getSimpleName())));
        }
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
