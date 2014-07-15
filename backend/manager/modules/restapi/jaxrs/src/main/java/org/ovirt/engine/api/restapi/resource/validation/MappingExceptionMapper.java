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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.restapi.utils.MappingException;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@Provider
public class MappingExceptionMapper implements ExceptionMapper<MappingException> {

    private static final Log LOGGER = LogFactory.getLog(MappingExceptionMapper.class);

    @Override
    public Response toResponse(MappingException exception) {
        LOGGER.error(exception);

        final Fault fault = new Fault();
        fault.setReason("Operation Failed");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(fault).build();
    }
}
