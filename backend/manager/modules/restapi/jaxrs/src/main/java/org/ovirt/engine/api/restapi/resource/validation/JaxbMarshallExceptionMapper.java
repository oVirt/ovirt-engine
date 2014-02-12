package org.ovirt.engine.api.restapi.resource.validation;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.plugins.providers.jaxb.JAXBMarshalException;
import org.ovirt.engine.api.model.Fault;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@ServerInterceptor
public class JaxbMarshallExceptionMapper implements ExceptionMapper<JAXBMarshalException> {

    @Context
    protected UriInfo uriInfo;
    @Context
    protected Request request;
    @Context
    protected Application application;

    @Override
    public Response toResponse(JAXBMarshalException ex) {
        try {
            Fault fault = new Fault();
            fault.setReason("Bad Request");
            fault.setDetail(ex.getCause().getMessage());
            return Response.status(Status.BAD_REQUEST)
                    .entity(fault)
                    .build();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.status(Status.INTERNAL_SERVER_ERROR).build());
        }
    }
}
