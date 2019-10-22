/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.restapi.utils.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class MappingExceptionMapper implements ExceptionMapper<MappingException> {
    private static final Logger log = LoggerFactory.getLogger(MappingExceptionMapper.class);

    @Context
    protected UriInfo uriInfo;

    @Context
    protected Request request;

    @Override
    public Response toResponse(MappingException exception) {
        log.error(
            "Mapping exception while processing \"{}\" request for path \"{}\"",
            request.getMethod(),
            uriInfo.getPath()
        );
        log.error("Exception", exception);

        final Fault fault = new Fault();
        fault.setReason("Operation Failed");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(fault).build();
    }
}
