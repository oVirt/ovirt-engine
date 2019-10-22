/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.core.utils.Ticketing.generateOTP;

import java.net.URI;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.model.GraphicsType;
import org.ovirt.engine.api.model.Ticket;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.compat.Guid;

public class BackendGraphicsConsoleHelper {

    private static final long DEFAULT_TICKET_EXPIRY = 120L * 60L; // 2 hours

    public static org.ovirt.engine.core.common.businessentities.GraphicsType asGraphicsType(String consoleId) {
        String consoleString = HexUtils.hex2string(consoleId);

        GraphicsType type = GraphicsType.fromValue(consoleString);
        return VmMapper.map(type, null);
    }

    public static String asConsoleId(org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType) {
        GraphicsType type = VmMapper.map(graphicsType, null);
        return HexUtils.string2hex(type.value());
    }

    public static GraphicsConsole get(Supplier<GraphicsConsoles> list, String consoleId) {
        return list.get().getGraphicsConsoles().stream()
            .filter(console -> consoleId.equals(console.getId()))
            .findFirst()
            .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build()));
    }

    public static Response remove(BackendResource resource, Guid guid, String consoleId) {
        List<GraphicsDevice> devices = DisplayHelper.getGraphicsDevicesForEntity(resource, guid, false);
        if (devices == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }

        org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType = asGraphicsType(consoleId);
        return devices.stream()
            .filter(device -> device.getGraphicsType().equals(graphicsType))
            .findFirst()
            .map(device -> resource.performAction(ActionType.RemoveGraphicsAndVideoDevices, new GraphicsParameters(device)))
            .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build()));
    }

    public static Map<org.ovirt.engine.core.common.businessentities.GraphicsType, GraphicsInfo> list(BackendResource resource, Guid guid) {
        Map<org.ovirt.engine.core.common.businessentities.GraphicsType, GraphicsInfo> graphicsTypeToGraphicsInfo;
        List<org.ovirt.engine.core.common.businessentities.GraphicsType> graphicsTypes =
            DisplayHelper.getGraphicsTypesForEntity(resource, guid, true);
        graphicsTypeToGraphicsInfo = new EnumMap<>(org.ovirt.engine.core.common.businessentities.GraphicsType.class);
        for (org.ovirt.engine.core.common.businessentities.GraphicsType type : graphicsTypes) {
            graphicsTypeToGraphicsInfo.put(type, null);
        }

        return graphicsTypeToGraphicsInfo;
    }

    public static Response find(GraphicsConsole console, Supplier<GraphicsConsoles> list) {
        return list.get().getGraphicsConsoles().stream()
            .filter(existing -> existing.getProtocol().equals(console.getProtocol()))
            .findFirst()
            .map(existing -> Response.created(URI.create(existing.getHref())).entity(existing).build())
            .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build()));
    }

    public static Response setTicket(BackendResource resource, Action action, Guid vmId,
                                     org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType) {
        final Response response = resource.performAction(ActionType.SetVmTicket,
                new SetVmTicketParameters(vmId,
                        getTicketValue(action),
                        getTicketExpiry(action),
                        graphicsType),
                action);

        final Action actionResponse = (Action) response.getEntity();

        if (CreationStatus.FAILED.value().equals(actionResponse.getStatus())) {
            actionResponse.getTicket().setValue(null);
            actionResponse.getTicket().setExpiry(null);
        }

        return response;
    }

    private static String getTicketValue(Action action) {
        if (!ensureTicket(action).isSetValue()) {
            action.getTicket().setValue(generateOTP());
        }
        return action.getTicket().getValue();
    }

    private static int getTicketExpiry(Action action) {
        if (!ensureTicket(action).isSetExpiry()) {
            action.getTicket().setExpiry(DEFAULT_TICKET_EXPIRY);
        }
        return action.getTicket().getExpiry().intValue();
    }

    private static Ticket ensureTicket(Action action) {
        if (!action.isSetTicket()) {
            action.setTicket(new Ticket());
        }
        return action.getTicket();
    }
}
