package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.restapi.utils.MappingException;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@Provider
@ServerInterceptor
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
