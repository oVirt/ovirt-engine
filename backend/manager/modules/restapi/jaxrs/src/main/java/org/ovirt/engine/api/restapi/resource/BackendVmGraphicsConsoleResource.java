package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsType;
import org.ovirt.engine.api.resource.ApiMediaType;
import org.ovirt.engine.api.resource.VmGraphicsConsoleResource;
import org.ovirt.engine.api.resource.VmGraphicsConsolesResource;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.ConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class BackendVmGraphicsConsoleResource
    extends BackendResource
    implements VmGraphicsConsoleResource {

    private VmGraphicsConsolesResource parent;
    private Guid vmGuid;
    private String consoleId;

    public BackendVmGraphicsConsoleResource(BackendVmGraphicsConsolesResource parent, Guid vmGuid, String consoleId) {
        this.parent = parent;
        this.vmGuid = vmGuid;
        this.consoleId = consoleId;
    }

    @Override
    public GraphicsConsole get() {
        List<String> supportedIds = new ArrayList<>();

        for (GraphicsConsole graphicsConsole : parent.list().getGraphicsConsoles()) {
            if (consoleId.equals(graphicsConsole.getId())) {
                return graphicsConsole;
            }

            supportedIds.add(graphicsConsole.getId());
        }

        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }

    @Override
    public Response generateDescriptor() {
        String consoleString = HexUtils.hex2string(consoleId);

        GraphicsConsole console = new GraphicsConsole();
        console.setProtocol(consoleString);
        validateEnums(GraphicsConsole.class, console);

        GraphicsType type = GraphicsType.valueOf(consoleString);

        org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType = getMappingLocator().getMapper(GraphicsType.class, org.ovirt.engine.core.common.businessentities.GraphicsType.class).map(type, null);

        ConsoleOptions consoleOptions = new ConsoleOptions(graphicsType);
        consoleOptions.setVmId(vmGuid);
        ConsoleOptions configuredOptions = runQuery(VdcQueryType.ConfigureConsoleOptions,
                new ConfigureConsoleOptionsParams(consoleOptions, true)).getReturnValue();

        String descriptor = runQuery(VdcQueryType.GetConsoleDescriptorFile, new ConsoleOptionsParams(configuredOptions))
                .getReturnValue();
        Response.ResponseBuilder builder = Response.ok(descriptor.getBytes(Charset.forName("UTF-8")), ApiMediaType.APPLICATION_X_VIRT_VIEWER);

        return builder.build();
    }
}
