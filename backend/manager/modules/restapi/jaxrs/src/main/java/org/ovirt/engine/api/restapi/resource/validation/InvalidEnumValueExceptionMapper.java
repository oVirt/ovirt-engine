package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.utils.InvalidEnumValueException;

@Provider
public class InvalidEnumValueExceptionMapper implements ExceptionMapper<InvalidEnumValueException>{

    @Override
    public Response toResponse(InvalidEnumValueException exception) {
        Fault fault = new Fault();
        fault.setReason("Invalid Enum value");
        fault.setDetail(exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(fault).build();
    }

}
