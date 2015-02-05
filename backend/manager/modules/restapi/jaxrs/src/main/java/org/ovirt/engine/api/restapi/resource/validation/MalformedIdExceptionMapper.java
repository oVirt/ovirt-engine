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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ovirt.engine.api.restapi.utils.MalformedIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class MalformedIdExceptionMapper implements ExceptionMapper<MalformedIdException> {
    private static final Logger log = LoggerFactory.getLogger(MalformedIdExceptionMapper.class);

    @Context
    protected UriInfo uriInfo;

    @Context
    protected Request request;

    @Override
    public Response toResponse(MalformedIdException exception) {
        log.error(
            "Malformed id detected while processing \"{}\" request for path \"{}\"",
            request.getMethod(),
            uriInfo.getPath()
        );
        log.error("Exception", exception);
        return Response.status(Status.BAD_REQUEST).entity(exception.getCause().getMessage()).build();
    }
}
