package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.ovirt.engine.api.restapi.utils.MalformedIdException;

@Provider
@ServerInterceptor
public class MalformedIdExceptionMapper implements ExceptionMapper<MalformedIdException> {

    @Override
    public Response toResponse(MalformedIdException exception) {
        return Response.status(Status.BAD_REQUEST).entity(exception.getCause().getMessage()).build();
    }
}
