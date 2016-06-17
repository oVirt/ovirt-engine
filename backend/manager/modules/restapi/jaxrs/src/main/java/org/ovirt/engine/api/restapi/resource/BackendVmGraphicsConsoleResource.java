package org.ovirt.engine.api.restapi.resource;

import java.nio.charset.StandardCharsets;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.GraphicsType;
import org.ovirt.engine.api.model.ProxyTicket;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ApiMediaType;
import org.ovirt.engine.api.resource.VmGraphicsConsoleResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.ConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.GetSignedWebsocketProxyTicketParams;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendVmGraphicsConsoleResource
    extends BackendGraphicsConsoleResource
    implements VmGraphicsConsoleResource {

    private static final Logger log = LoggerFactory.getLogger(BackendVmGraphicsConsoleResource.class);

    public BackendVmGraphicsConsoleResource(BackendVmGraphicsConsolesResource parent, Guid vmGuid, String consoleId) {
        super(parent, vmGuid, consoleId);
    }

    /**
     * A method handling GET requests with media type x-virt-viewer.
     * Returns a console representation usable by virt-viewer client (e.g. a .vv file)
     *
     * @return a console representation for virt-viewer (e.g. a .vv file)
     */
    @GET
    @Produces({ApiMediaType.APPLICATION_X_VIRT_VIEWER})
    public Response generateDescriptor() {
        org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType = asGraphicsType();

        ConsoleOptions consoleOptions = new ConsoleOptions(graphicsType);
        consoleOptions.setVmId(getGuid());
        VdcQueryReturnValue configuredOptionsReturnValue = runQuery(VdcQueryType.ConfigureConsoleOptions,
                new ConfigureConsoleOptionsParams(consoleOptions, true));
        if (!configuredOptionsReturnValue.getSucceeded()) {
            return handleConfigureConsoleError(configuredOptionsReturnValue);
        }

        VdcQueryReturnValue consoleDescriptorReturnValue = runQuery(VdcQueryType.GetConsoleDescriptorFile,
                new ConsoleOptionsParams(configuredOptionsReturnValue.getReturnValue()));

        Response.ResponseBuilder builder;
        if (consoleDescriptorReturnValue.getSucceeded() && consoleDescriptorReturnValue.getReturnValue() != null) {
            builder = Response.ok(((String) consoleDescriptorReturnValue.getReturnValue())
                    .getBytes(StandardCharsets.UTF_8), ApiMediaType.APPLICATION_X_VIRT_VIEWER);
        } else {
            builder = Response.noContent();
        }

        return builder.build();
    }

    private Response handleConfigureConsoleError(VdcQueryReturnValue configuredOptionsReturnValue) {
        log.error(localize(Messages.BACKEND_FAILED_TEMPLATE, configuredOptionsReturnValue.getExceptionString()));
        if (EngineMessage.USER_CANNOT_FORCE_RECONNECT_TO_VM.name()
                .equals(configuredOptionsReturnValue.getExceptionString())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.serverError().build();
    }

    @Override
    public Response proxyTicket(Action action) {
        final String plainConsoleId = HexUtils.hex2string(getConsoleId());
        final GraphicsType graphicsTypeModel = GraphicsType.fromValue(plainConsoleId);
        final org.ovirt.engine.core.common.businessentities.GraphicsType graphicsTypeEntity =
                VmMapper.map(graphicsTypeModel, null);

        final String ticketValue = getTicket(graphicsTypeEntity);
        if (!action.isSetProxyTicket()) {
            action.setProxyTicket(new ProxyTicket());
        }
        action.getProxyTicket().setValue(ticketValue);
        return Response.ok().entity(action).build();
    }

    private String getTicket(org.ovirt.engine.core.common.businessentities.GraphicsType graphicsTypeEntity) {
        final GetSignedWebsocketProxyTicketParams params =
                new GetSignedWebsocketProxyTicketParams(getGuid(), graphicsTypeEntity);
        final VdcQueryReturnValue ticketQueryReturnValue =
                runQuery(VdcQueryType.GetSignedWebsocketProxyTicket, params);
        if (!ticketQueryReturnValue.getSucceeded()) {
            try {
                backendFailure(ticketQueryReturnValue.getExceptionString());
            } catch (BackendFailureException ex) {
                handleError(ex, false);
            }
        }
        return ticketQueryReturnValue.getReturnValue();
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }
}
