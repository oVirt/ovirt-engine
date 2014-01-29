package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.utils.InvalidValueException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidValueExceptionMapper implements ExceptionMapper<InvalidValueException> {

    @Override
    public Response toResponse(InvalidValueException e) {
        Fault fault = new Fault();
        fault.setReason("Invalid Value");
        fault.setDetail(e.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(fault).build();
    }
}
