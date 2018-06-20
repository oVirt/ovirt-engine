package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendGraphicsConsoleHelper.asGraphicsType;

import java.nio.charset.StandardCharsets;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.GraphicsConsole;
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
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendVmGraphicsConsoleResource
    extends AbstractBackendActionableResource<GraphicsConsole, org.ovirt.engine.core.common.businessentities.GraphicsType>
    implements VmGraphicsConsoleResource {

    private static final Logger log = LoggerFactory.getLogger(BackendVmGraphicsConsoleResource.class);

    private BackendVmGraphicsConsolesResource parent;
    private Guid guid;
    private String consoleId;

    public BackendVmGraphicsConsoleResource(BackendVmGraphicsConsolesResource parent, Guid vmGuid, String consoleId) {
        super(consoleId, GraphicsConsole.class, org.ovirt.engine.core.common.businessentities.GraphicsType.class);
        this.parent = parent;
        this.guid = vmGuid;
        this.consoleId = consoleId;
    }

    private QueryReturnValue generateDescriptorResponse() throws Exception {
        org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType =
            BackendGraphicsConsoleHelper.asGraphicsType(consoleId);

        ConsoleOptions consoleOptions = new ConsoleOptions(graphicsType);
        consoleOptions.setVmId(guid);
        QueryReturnValue configuredOptionsReturnValue = runQuery(
            QueryType.ConfigureConsoleOptions,
            new ConfigureConsoleOptionsParams(consoleOptions, true)
        );
        if (!configuredOptionsReturnValue.getSucceeded()) {
            QueryReturnValue returnValue = new QueryReturnValue();
            returnValue.setSucceeded(false);
            returnValue.setExceptionString(configuredOptionsReturnValue.getExceptionString());
            return returnValue;
        }

        return runQuery(
            QueryType.GetConsoleDescriptorFile,
            new ConsoleOptionsParams(configuredOptionsReturnValue.getReturnValue())
        );
    }

    /**
     * A method handling GET requests with media type x-virt-viewer.
     * Returns a console representation usable by virt-viewer client (e.g. a .vv file)
     *
     * @return a console representation for virt-viewer (e.g. a .vv file)
     */
    @GET
    @Produces(ApiMediaType.APPLICATION_X_VIRT_VIEWER)
    public Response generateDescriptor() {
        try {
            QueryReturnValue consoleDescriptorReturnValue = generateDescriptorResponse();
            Response.ResponseBuilder builder;
            if (consoleDescriptorReturnValue.getSucceeded() && consoleDescriptorReturnValue.getReturnValue() != null) {
                builder = Response.ok(((String) consoleDescriptorReturnValue.getReturnValue())
                        .getBytes(StandardCharsets.UTF_8), ApiMediaType.APPLICATION_X_VIRT_VIEWER);
            } else {
                Fault fault = new Fault();
                fault.setReason(consoleDescriptorReturnValue.getExceptionString());
                builder = Response.status(Response.Status.CONFLICT).type(ApiMediaType.APPLICATION_XML).entity(fault);
            }
            return builder.build();
        } catch (Exception ex) {
            return handleConfigureConsoleError(ex.getMessage());
        }
    }

    @Override
    public Response remoteViewerConnectionFile(Action action) {
        try {
            QueryReturnValue consoleDescriptorReturnValue = generateDescriptorResponse();
            Response.ResponseBuilder builder;
            if (consoleDescriptorReturnValue.getSucceeded() && consoleDescriptorReturnValue.getReturnValue() != null) {
                action.setRemoteViewerConnectionFile(consoleDescriptorReturnValue.getReturnValue());
                builder = Response.ok().entity(action);
            } else {
                builder = Response.noContent();
            }
            return builder.build();
        } catch (Exception ex) {
            return handleConfigureConsoleError(ex.getMessage());
        }
    }

    private Response handleConfigureConsoleError(String exceptionMessage) {
        log.error(localize(Messages.BACKEND_FAILED_TEMPLATE, exceptionMessage));
        if (EngineMessage.USER_CANNOT_FORCE_RECONNECT_TO_VM.name().equals(exceptionMessage)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.serverError().build();
    }

    @Override
    public Response ticket(Action action) {
        return BackendGraphicsConsoleHelper.setTicket(this, action, guid, asGraphicsType(consoleId));
    }

    @Override
    public Response proxyTicket(Action action) {
        final String plainConsoleId = HexUtils.hex2string(consoleId);
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
                new GetSignedWebsocketProxyTicketParams(guid, graphicsTypeEntity);
        final QueryReturnValue ticketQueryReturnValue =
                runQuery(QueryType.GetSignedWebsocketProxyTicket, params);
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

    @Override
    public GraphicsConsole get() {
        return BackendGraphicsConsoleHelper.get(parent::list, consoleId);
    }

    @Override
    public Response remove() {
        return BackendGraphicsConsoleHelper.remove(this, guid, consoleId);
    }

    @Override
    protected Guid asGuidOr404(String id) {
        return null;
    }
}
