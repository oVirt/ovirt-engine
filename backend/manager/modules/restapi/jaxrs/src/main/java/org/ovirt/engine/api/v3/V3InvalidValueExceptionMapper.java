/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ovirt.engine.api.utils.InvalidValueException;
import org.ovirt.engine.api.v3.types.V3Fault;

@Provider
public class V3InvalidValueExceptionMapper implements ExceptionMapper<InvalidValueException> {
    @Override
    public Response toResponse(InvalidValueException exception) {
        V3Fault fault = new V3Fault();
        fault.setReason("Invalid value");
        fault.setDetail(exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(fault).build();
    }
}
