package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;

@Provider
@ServerInterceptor
public class JaxbExceptionMapper implements ExceptionMapper<JAXBException> {

    @Context
    protected UriInfo uriInfo;
    @Context
    protected Request request;
    @Context
    protected Application application;

    @Override
    public Response toResponse(JAXBException exception) {
        try {
            return Response.status(Status.BAD_REQUEST)
                    .entity(new UsageFinder().getUsageMessage(application, uriInfo, request))
                    .build();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }
    }
}
