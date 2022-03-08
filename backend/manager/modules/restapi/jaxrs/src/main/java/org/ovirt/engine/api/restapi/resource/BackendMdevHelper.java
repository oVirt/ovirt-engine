package org.ovirt.engine.api.restapi.resource;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.VmMediatedDevice;
import org.ovirt.engine.api.model.VmMediatedDevices;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MdevParameters;
import org.ovirt.engine.core.common.businessentities.VmMdevType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendMdevHelper {

    public static VmMediatedDevices list(AbstractBackendCollectionResource<VmMediatedDevice, VmMdevType> resource,
            Guid vmId) {
        VmMediatedDevices mdevs = new VmMediatedDevices();
        List<VmMdevType> entities = resource.getBackendCollection(QueryType.GetMdevs, new IdQueryParameters(vmId));
        for (VmMdevType entity : entities) {
            mdevs.getVmMediatedDevices().add(resource.addLinks(resource.map(entity)));
        }
        return mdevs;
    }

    public static VmMediatedDevice get(Supplier<VmMediatedDevices> list, String mdevId) {
        return list.get().getVmMediatedDevices().stream().filter(mdev -> mdevId.equals(mdev.getId())).findFirst()
                .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build()));
    }

    public static <T> Response add(AbstractBackendCollectionResource<VmMediatedDevice, T> resource,
            Supplier<VmMediatedDevices> list, VmMediatedDevice mdev, Guid vmId, boolean isVm) {
        VmMdevType device = resource.getMapper(VmMediatedDevice.class, VmMdevType.class).map(mdev, null);
        device.setVmId(vmId);
        MdevParameters parameters = new MdevParameters(device, isVm);
        ActionReturnValue res = resource.doCreateEntity(ActionType.AddMdev, parameters);
        if (res != null && res.getSucceeded()) {
            return list.get().getVmMediatedDevices().stream()
                    .filter(existing -> existing.getId().equals(device.getDeviceId().toString())).findFirst()
                    .map(existing -> Response.created(URI.create(existing.getHref())).entity(existing).build())
                    .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build()));
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }

    public static VmMdevType lookup(BackendResource resource, Guid vmId, String mdevId, boolean isVm) {
        List<VmMdevType> mdevs = resource.getEntity(List.class, QueryType.GetMdevs, new IdQueryParameters(vmId),
                mdevId.toString(), true);
        if (mdevs == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
        return mdevs.stream().filter(device -> mdevId.equals(device.getDeviceId().toString())).findFirst()
                .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build()));
    }

    public static Response remove(BackendResource resource, Guid vmId, String mdevId, boolean isVm) {
        VmMdevType device = lookup(resource, vmId, mdevId, isVm);
        return resource.performAction(ActionType.RemoveMdev, new MdevParameters(device, isVm));
    }
}
