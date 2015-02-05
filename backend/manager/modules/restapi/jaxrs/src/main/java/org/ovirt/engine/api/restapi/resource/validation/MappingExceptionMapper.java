/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
