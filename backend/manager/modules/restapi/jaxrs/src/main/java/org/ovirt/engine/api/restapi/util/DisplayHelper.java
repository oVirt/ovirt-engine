package org.ovirt.engine.api.restapi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.api.restapi.types.DisplayMapper;
import org.ovirt.engine.core.common.action.HasGraphicsDevices;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.CertificateSubjectHelper;

public class DisplayHelper {

    private DisplayHelper() { }

    /**
     * Returns graphics types of graphics devices of entity with given id.
     */
    public static List<GraphicsType> getGraphicsTypesForEntity(BackendResource backendResource, Guid id, boolean nextRun) {
        return getGraphicsTypesForEntity(backendResource, id, null, nextRun);
    }

    public static List<GraphicsType> getGraphicsTypesForEntity(BackendResource backendResource, Guid id,
            Map<Guid, List<GraphicsDevice>> cache, boolean nextRun) {
        List<GraphicsType> graphicsTypes = new ArrayList<>();
        List<GraphicsDevice> graphicsDevices;

        if (cache == null) {
            graphicsDevices = getGraphicsDevicesForEntity(backendResource, id, nextRun);
        } else {
            graphicsDevices = cache.get(id);
        }

        if (graphicsDevices != null) {
            for (GraphicsDevice graphicsDevice : graphicsDevices) {
                graphicsTypes.add(graphicsDevice.getGraphicsType());
            }
        }

        return graphicsTypes;
    }

    public static List<GraphicsDevice> getGraphicsDevicesForEntity(BackendResource backendResource, Guid id,
                                                                   boolean nextRun) {
        List<GraphicsDevice> graphicsDevices = backendResource.getEntity(List.class,
                nextRun ? QueryType.GetNextRunGraphicsDevices : QueryType.GetGraphicsDevices,
                new IdQueryParameters(id),
                id.toString(), true);

        return graphicsDevices;
    }

    public static Map<Guid, List<GraphicsDevice>> getGraphicsDevicesForMultipleEntities(
            BackendResource backendResource, List<Guid> vmIds) {
        Map<Guid, List<GraphicsDevice>> graphicsDevices = backendResource.getEntity(Map.class,
                QueryType.GetGraphicsDevicesMultiple,
                new IdsQueryParameters(vmIds),
                "GetGraphicsDevicesMultiple", true);
        return graphicsDevices;
    }

    /**
     * Set data about graphics from (REST) Template to parameters.
     *
     * @param display - display that contains graphics data
     * @param params  - parameters to be updated with graphics data
     */
    public static void setGraphicsToParams(Display display, HasGraphicsDevices params) {
        if (display != null && display.isSetType()) {
            DisplayType newDisplayType = display.getType();

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
     */
    public static void adjustDisplayData(BackendResource res, Template template) {
        adjustDisplayDataInternal(res, template, null, false);
    }

    /**
     * Sets static display info (derived from graphics device) to the VM.
     * Serves for BC purposes as VM can have more graphics devices, but old restapi allows us to set only one.
     * If there are multiple graphics, SPICE is preferred.
     */
    public static void adjustDisplayData(BackendResource res, Vm vm, boolean nextRun) {
        adjustDisplayData(res, vm, null, nextRun);
    }

    public static void adjustDisplayData(BackendResource res, Vm vm, Map<Guid, List<GraphicsDevice>> vmsGraphicsDevices,
                                         boolean nextRun) {
        adjustDisplayDataInternal(res, vm, vmsGraphicsDevices, nextRun);
    }

    private static void adjustDisplayDataInternal(BackendResource backendResource, BaseResource res,
            Map<Guid, List<GraphicsDevice>> vmsGraphicsDevices, boolean nextRun) {
        Display display = extractDisplayFromResource(res);

        if (display != null && !display.isSetType()) {
            List<GraphicsType> graphicsTypes = getGraphicsTypesForEntity(backendResource,
                    new Guid(res.getId()), vmsGraphicsDevices, nextRun);

            if (graphicsTypes.contains(GraphicsType.SPICE)) {
                display.setType(DisplayType.SPICE);
            } else if (graphicsTypes.contains(GraphicsType.VNC)) {
                display.setType(DisplayType.VNC);
            } else {
                resetDisplay(res);
            }
        }
    }

    public static void addDisplayCertificate(BackendResource res, Vm vm) {
        QueryReturnValue result =
                res.runQuery(QueryType.GetVdsCertificateSubjectByVmId,
                             new IdQueryParameters(new Guid(vm.getId())));

        if (result != null && result.getSucceeded() && result.getReturnValue() != null) {
            if (!vm.isSetDisplay()) {
                vm.setDisplay(new Display());
            }
            vm.getDisplay().setCertificate(new Certificate());
            vm.getDisplay().getCertificate().setSubject(result.getReturnValue().toString());
            vm.getDisplay().getCertificate().setOrganization(CertificateSubjectHelper.getOrganizationName());

            final QueryReturnValue caCertificateReturnValue =
                    res.runQuery(QueryType.GetCACertificate, new QueryParametersBase());
            if (caCertificateReturnValue.getSucceeded()) {
                vm.getDisplay().getCertificate().setContent(caCertificateReturnValue.getReturnValue());
            }
        }
    }

    private static Display extractDisplayFromResource(BaseResource res) {
        if (res instanceof Vm) {
            return ((Vm) res).getDisplay();
        }
        if (res instanceof Template) {
            return ((Template) res).getDisplay();
        }
        return null;
    }

    private static void resetDisplay(BaseResource res) {
        if (res instanceof Vm) {
            ((Vm) res).setDisplay(null);
        }
        if (res instanceof Template) {
            ((Template) res).setDisplay(null);
        }
    }

}
