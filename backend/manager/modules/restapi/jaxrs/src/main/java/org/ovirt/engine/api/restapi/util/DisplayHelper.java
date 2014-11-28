package org.ovirt.engine.api.restapi.util;

import java.util.ArrayList;
import java.util.List;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.api.restapi.types.DisplayMapper;
import org.ovirt.engine.core.common.action.HasGraphicsDevices;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class DisplayHelper {

    private DisplayHelper() { }

    /**
     * Returs graphics types of graphics devices of entity with given id.
     */
    public static List<GraphicsType> getGraphicsTypesForEntity(BackendResource backendResource, Guid id) {
        List<GraphicsType> graphicsTypes = new ArrayList<>();

        List<GraphicsDevice> graphicsDevices = backendResource.getEntity(List.class,
                VdcQueryType.GetGraphicsDevices,
                new IdQueryParameters(id),
                "GetGraphicsDevices", true);

        if (graphicsDevices != null) {
            for (GraphicsDevice graphicsDevice : graphicsDevices) {
                graphicsTypes.add(graphicsDevice.getGraphicsType());
            }
        }

        return graphicsTypes;
    }

    /**
     * Set data about graphics from (REST) Template to parameters.
     *
     * @param display - display that contains graphics data
     * @param params  - parameters to be updated with graphics data
     */
    public static void setGraphicsToParams(Display display, HasGraphicsDevices params) {
        if (display != null && display.isSetType()) {
            DisplayType newDisplayType = DisplayType.fromValue(display.getType());

            if (newDisplayType != null) {
                for (GraphicsType graphicsType : GraphicsType.values()) {
                    params.getGraphicsDevices().put(graphicsType, null); // reset graphics devices
                }

                GraphicsType newGraphicsType = DisplayMapper.map(newDisplayType, null);
                params.getGraphicsDevices().put(newGraphicsType,
                        new GraphicsDevice(newGraphicsType.getCorrespondingDeviceType()));
            }
        }
    }

    /**
     * Sets static display info (derived from graphics device) to the Template.
     * Serves for BC purposes as VM can have more graphics devices, but old restapi allows us to set only one.
     * If there are multiple graphics, SPICE is preferred.
     *
     * @param template
     */
    public static void adjustDisplayData(BackendResource res, Template template) {
        adjustDisplayDataInternal(res, template);
    }

    /**
     * Sets static display info (derived from graphics device) to the VM.
     * Serves for BC purposes as VM can have more graphics devices, but old restapi allows us to set only one.
     * If there are multiple graphics, SPICE is preferred.
     *
     * @param vm
     */
    public static void adjustDisplayData(BackendResource res, VM vm) {
        adjustDisplayDataInternal(res, vm);
    }

    private static void adjustDisplayDataInternal(BackendResource backendResource, BaseResource res) {
        Display display = extractDisplayFromResource(res);

        if (display != null && !display.isSetType()) {
            List<GraphicsType> graphicsTypes = getGraphicsTypesForEntity(backendResource, new Guid(res.getId()));

            if (graphicsTypes.contains(GraphicsType.SPICE)) {
                display.setType(DisplayType.SPICE.value());
            } else if (graphicsTypes.contains(GraphicsType.VNC)) {
                display.setType(DisplayType.VNC.value());
            } else {
                resetDisplay(res);
            }
        }
    }

    private static Display extractDisplayFromResource(BaseResource res) {
        if (res instanceof VM) {
            return ((VM) res).getDisplay();
        }
        if (res instanceof Template) {
            return ((Template) res).getDisplay();
        }
        return null;
    }

    private static void resetDisplay(BaseResource res) {
        if (res instanceof VM) {
            ((VM) res).setDisplay(null);
        }
        if (res instanceof Template) {
            ((Template) res).setDisplay(null);
        }
    }

}
