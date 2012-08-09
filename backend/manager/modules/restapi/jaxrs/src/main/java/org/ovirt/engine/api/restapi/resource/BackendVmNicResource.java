package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.VmNicResource;
import org.ovirt.engine.core.common.action.HotPlugUnplugVmNicParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.PortMirroring;

public class BackendVmNicResource extends BackendNicResource implements VmNicResource {

    protected BackendVmNicResource(String id,
                                   AbstractBackendReadOnlyDevicesResource<NIC, Nics, VmNetworkInterface> collection,
                                   VdcActionType updateType,
                                   ParametersProvider<NIC, VmNetworkInterface> updateParametersProvider,
                                   String[] requiredUpdateFields,
                                   String[] subCollections) {
        super(id, collection, updateType, updateParametersProvider, requiredUpdateFields, subCollections);
    }

    @Override
    protected NIC populate(NIC model, VmNetworkInterface entity) {
        BackendVmNicsResource parent = (BackendVmNicsResource)collection;
        Guid clusterId = parent.getClusterId();
        org.ovirt.engine.core.common.businessentities.Network network = parent.getClusterNetwork(clusterId, null, model.getNetwork().getName());
        String networkId = network == null ? null : network.getId().toString();
        model.getNetwork().setId(networkId);
        model.getNetwork().setName(null);
        if (entity.isPortMirroring()) {
            PortMirroring portMirroring = new PortMirroring();
            Networks networks = new Networks();
            Network net = new Network();
            net.setId(networkId);
            portMirroring.setNetworks(networks);
            portMirroring.getNetworks().getNetworks().add(net);
            model.setPortMirroring(portMirroring);
        }
        return parent.addStatistics(model, entity, uriInfo, httpHeaders);
    }

    @Override
    public NIC update(NIC device) {
        //TODO: this is temporary mapping between engine boolean port mirroring parameter, and REST
        //      port mirroring network collection, next engine version will support the network collection
        //      in port mirroring
        validateEnums(NIC.class, device);
        boolean fault = false;
        String faultString = null;
        boolean isPortMirroring = device.isSetPortMirroring() && device.getPortMirroring().isSetNetworks();
        boolean isPortMirroringExceeded =
                isPortMirroring && device.getPortMirroring().getNetworks().getNetworks().size() > 1;
        isPortMirroring = isPortMirroring && device.getPortMirroring().getNetworks().getNetworks().size() == 1;

        // Check if user set more then one network in port mirroring networks collection
        if (isPortMirroringExceeded) {
            fault = true;
            faultString = "Cannot set more than one network in port mirroring mode";
        }

        if (!fault && isPortMirroring) {
            boolean isNetwork = device.isSetNetwork() && device.getNetwork().isSetId();
            // if Nics network set no need to check the Nics network from the engine
            if (isNetwork) {
                if (!device.getNetwork()
                        .getId()
                        .equals(device.getPortMirroring().getNetworks().getNetworks().get(0).getId())) {
                    fault = true;
                    faultString = "The port mirroring network must match the Network set on the NIC";
                }
                // check if port mirroring network match the Nics network that is store in the engine
            } else if (!device.getPortMirroring()
                    .getNetworks()
                    .getNetworks()
                    .get(0)
                    .getId()
                    .equals(get().getNetwork().getId())) {
                fault = true;
                faultString = "The port mirroring network must match the Network set on the NIC";
            }
        }

        if (fault) {
            Fault f = new Fault();
            f.setReason(faultString);
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(f).build();
            throw new WebApplicationException(response);
        }
        return super.update(device);
    }

    @Override
    public ActionResource getActionSubresource(String action, String oid) {
        return null;
    }

    @Override
    public Response activate(Action action) {
        HotPlugUnplugVmNicParameters params = new HotPlugUnplugVmNicParameters(guid, PlugAction.PLUG);
        BackendNicsResource parent = (BackendNicsResource) collection;
        params.setVmId(parent.parentId);
        return doAction(VdcActionType.HotPlugUnplugVmNic, params, action);
    }

    @Override
    public Response deactivate(Action action) {
        HotPlugUnplugVmNicParameters params = new HotPlugUnplugVmNicParameters(guid, PlugAction.UNPLUG);
        params.setVmId(((BackendNicsResource) collection).parentId);
        return doAction(VdcActionType.HotPlugUnplugVmNic, params, action);
    }

    @Override
    public NIC get() {
        return super.get();//explicit call solves REST-Easy confusion
    }
}
