/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

@Provider
public class JsonExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    private static final Logger log = LoggerFactory.getLogger(JsonExceptionMapper.class);

    @Context
    protected UriInfo uriInfo;

    @Context
    protected Request request;

    @Override
    public Response toResponse(JsonProcessingException exception) {
        try {
            log.error(
                "JSON exception while processing \"{}\" request for path \"{}\"",
                request.getMethod(),
                uriInfo.getPath()
            );
            log.error("Exception", exception);
            return Response.status(Status.BAD_REQUEST)
                    .entity(new UsageFinder().getUsageMessage(uriInfo, request))
                    .build();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }
    }
}
