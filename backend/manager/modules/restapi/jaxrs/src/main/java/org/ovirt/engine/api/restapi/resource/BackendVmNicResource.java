package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.PortMirroring;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.VmNicResource;
import org.ovirt.engine.api.resource.VmReportedDevicesResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

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
    protected NIC doPopulate(NIC model, VmNetworkInterface entity) {
        BackendVmNicsResource parent = (BackendVmNicsResource) collection;
        parent.addReportedDevices(model, entity);
        return model;
    }

    @Override
    protected NIC deprecatedPopulate(NIC model, VmNetworkInterface entity) {
        BackendVmNicsResource parent = (BackendVmNicsResource) collection;
        org.ovirt.engine.core.common.businessentities.network.Network network = null;
        String networkId = null;
        if (model.isSetNetwork() && model.getNetwork().isSetName()) {
            Guid clusterId = parent.getClusterId();
            network = parent.getClusterNetwork(clusterId, null, model.getNetwork().getName());
            networkId = network == null ? null : network.getId().toString();
            model.getNetwork().setId(networkId);
            model.getNetwork().setName(null);
        }

        if (entity.isPortMirroring()) {
            PortMirroring portMirroring = new PortMirroring();
            Networks networks = new Networks();
            if (network != null) {
                Network net = new Network();
                net.setId(networkId);
                net.setName(network.getName());
                portMirroring.setNetworks(networks);
                portMirroring.getNetworks().getNetworks().add(net);
            }

            model.setPortMirroring(portMirroring);
        }
        parent.addStatistics(model, entity);
        return model;
    }

    @Override
    public NIC update(NIC device) {
        validateEnums(NIC.class, device);
        if (device.isSetPortMirroring() || device.isSetNetwork()) {
            validatePortMirroring(device);
        }

        return super.update(device);
    }

    private void validatePortMirroring(NIC device) {
        //TODO: this is temporary mapping between engine boolean port mirroring parameter, and REST
        //      port mirroring network collection, next engine version will support the network collection
        //      in port mirroring
        NIC nic = null;
        boolean fault = false;
        String faultString = "The port mirroring network must match the Network set on the NIC";
        Network pmNetwork = null;

        boolean isPortMirroring = false;
        if (device.isSetPortMirroring()) {
            if (device.getPortMirroring().isSetNetworks()) {
                boolean isPortMirroringExceeded =
                        device.getPortMirroring().getNetworks().getNetworks().size() > 1;
                if (isPortMirroringExceeded) {
                    fault = true;
                    faultString = "Cannot set more than one network in port mirroring mode";
                }
                if (device.getPortMirroring().getNetworks().getNetworks().size() == 1) {
                    isPortMirroring = true;
                    pmNetwork = device.getPortMirroring().getNetworks().getNetworks().get(0);
                }
            }
        } else {
            nic = get();
            isPortMirroring =
                    nic.isSetPortMirroring() && nic.getPortMirroring().isSetNetworks()
                            && nic.getPortMirroring().getNetworks().isSetNetworks();
            if (isPortMirroring) {
                pmNetwork = nic.getPortMirroring().getNetworks().getNetworks().get(0);
            }
        }

        if (!fault && isPortMirroring) {
            String networkId =
                    (device.isSetNetwork() && device.getNetwork().isSetId()) ? device.getNetwork().getId() : null;
            String networkName =
                    (device.isSetNetwork() && device.getNetwork().isSetName()) ? device.getNetwork().getName() : null;
            if (!(device.isSetNetwork() && device.getNetwork().getId() == null && device.getNetwork().getName() == null)) {
                String pmNetworkId = (pmNetwork.isSetId() ? pmNetwork.getId() : null);
                String pmNetworkName = (pmNetwork.isSetName() ? pmNetwork.getName() : null);
                if (pmNetworkId != null) {
                    if (networkId == null) {
                        if (networkName != null) {
                            networkId = getNetworkId(networkName);
                        } else {
                            nic = nic != null ? nic : get();
                            networkId = nic.getNetwork().getId();
                        }
                    }
                    if (networkId != null) {
                        fault = (!networkId.equals(pmNetworkId));
                    }
                } else if (pmNetworkName != null) {
                    if (networkName == null) {
                        if (networkId == null) {
                            nic = nic != null ? nic : get();
                            networkId = nic.getNetwork().getId();
                        }
                        pmNetworkId = getNetworkId(pmNetworkName);
                        if (networkId != null) {
                            fault = (!networkId.equals(pmNetworkId));
                        }
                    }
                    if (networkId != null || networkName != null) {
                        fault = fault || (!pmNetworkName.equals(networkName));
                    }
                } else {
                    fault = true;
                    faultString = "Network must have name or id property for port mirroring";
                }
            }
        }
        if (fault) {
            Fault f = new Fault();
            f.setReason(faultString);
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(f).build();
            throw new WebApplicationException(response);
        }
    }

    private String getNetworkId(String networkName) {
        if (networkName != null) {
            BackendVmNicsResource parent = (BackendVmNicsResource) collection;
            Guid clusterId = parent.getClusterId();
            org.ovirt.engine.core.common.businessentities.network.Network n =
                    parent.getClusterNetwork(clusterId, null, networkName);
            if (n != null) {
                return n.getId().toString();
            }
        }
        return null;
    }

    @Override
    public ActionResource getActionSubresource(String action, String oid) {
        return null;
    }

    @Override
    public Response activate(Action action) {
        NIC nic = get();
        nic.setPlugged(true);
        update(nic);
        return actionSuccess(action);
    }

    @Override
    public Response deactivate(Action action) {
        NIC nic = get();
        nic.setPlugged(false);
        update(nic);
        return actionSuccess(action);
    }

    @Override
    public NIC get() {
        return super.get();//explicit call solves REST-Easy confusion
    }

    @Override
    public VmReportedDevicesResource getVmReportedDevicesResource() {
        return inject(new BackendVmReportedDevicesResource(guid));
    }
}
