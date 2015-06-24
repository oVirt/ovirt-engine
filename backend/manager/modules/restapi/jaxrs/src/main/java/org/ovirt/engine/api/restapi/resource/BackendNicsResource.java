package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.PortMirroring;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendNicsResource
        extends AbstractBackendDevicesResource<NIC, Nics, VmNetworkInterface>
        implements DevicesResource<NIC, Nics> {

    static final String SUB_COLLECTIONS = "statistics";

    public BackendNicsResource(Guid parentId,
                               VdcQueryType queryType,
                               VdcQueryParametersBase queryParams,
                               VdcActionType addAction,
                               VdcActionType updateAction) {
        super(NIC.class,
              Nics.class,
              VmNetworkInterface.class,
              parentId,
              queryType,
              queryParams,
              addAction,
              updateAction,
              SUB_COLLECTIONS);
    }

    @Override
    public Nics list() {
        Nics nics = new Nics();
        List<VmNetworkInterface> entities = getBackendCollection(queryType, queryParams);
        Guid clusterId = getClusterId();
        List<org.ovirt.engine.core.common.businessentities.network.Network> networks = getBackendCollection(org.ovirt.engine.core.common.businessentities.network.Network.class,
             VdcQueryType.GetAllNetworksByClusterId,
             new IdQueryParameters(clusterId));
        for (VmNetworkInterface entity : entities) {
            org.ovirt.engine.core.common.businessentities.network.Network network = null;
            if (entity.getNetworkName() != null) {
                network = lookupClusterNetwork(clusterId, null, entity.getNetworkName(), networks);
            }

            NIC nic = populate(map(entity), entity);
            if (network != null && network.getId() != null) {
                if (entity.isPortMirroring()) {
                    PortMirroring portMirroring = new PortMirroring();
                    Networks nets = new Networks();

                    Network net = new Network();
                    net.setId(network.getId().toString());
                    portMirroring.setNetworks(nets);
                    portMirroring.getNetworks().getNetworks().add(net);
                    nic.setPortMirroring(portMirroring);
                }
                nic.getNetwork().setId(network.getId().toString());
                nic.getNetwork().setName(null);
            }
            if (validate(nic)) {
                nics.getNics().add(addLinks(nic));
            }
        }
        return nics;
    }

    protected abstract Guid getClusterId();

    @Override
    protected <T> boolean matchEntity(VmNetworkInterface entity, T id) {
        return id != null && id.equals(entity.getId());
    }

    @Override
    protected boolean matchEntity(VmNetworkInterface entity, String name) {
        return name != null && name.equals(entity.getName());
    }

    @Override
    protected String[] getRequiredUpdateFields() {
        return new String[0];
    }

    @Override
    protected String[] getRequiredAddFields() {
        return new String[] { "name" };
    }

    protected org.ovirt.engine.core.common.businessentities.network.Network lookupClusterNetwork(Guid clusterId, Guid id, String name, List<org.ovirt.engine.core.common.businessentities.network.Network> networks) {
        for (org.ovirt.engine.core.common.businessentities.network.Network network : networks) {
            if ((id != null && id.equals(network.getId())) ||
                (name != null && name.equals(network.getName()))) {
                return network;
            }
        }
        return null;
    }

    protected org.ovirt.engine.core.common.businessentities.network.Network lookupClusterNetwork(Guid clusterId, Guid id, String name) {
        org.ovirt.engine.core.common.businessentities.network.Network net = getClusterNetwork(clusterId, id, name);
        if (net != null) {
            return net;
        }
        throw new WebFaultException(null, "Network not found in cluster", Response.Status.BAD_REQUEST);
    }

    protected org.ovirt.engine.core.common.businessentities.network.Network getClusterNetwork(Guid clusterId, Guid id, String name) {
        for (org.ovirt.engine.core.common.businessentities.network.Network entity : getBackendCollection(org.ovirt.engine.core.common.businessentities.network.Network.class,
                                                   VdcQueryType.GetAllNetworksByClusterId,
                                                   new IdQueryParameters(clusterId))) {
            if ((id != null && id.equals(entity.getId())) ||
                (name != null && name.equals(entity.getName()))) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public Response add(NIC device) {
        validateParameters(device, getRequiredAddFields());
        Response response = performCreate(addAction,
                                            getAddParameters(map(device), device),
                                            getEntityIdResolver(device.getName()));
        if (response!=null) {
            Object entity = response.getEntity();
            if (entity!=null) {
                NIC nic = (NIC)entity;
                setNetworkId(nic);
            }
        }
        return response;
    }

    protected void setNetworkId(NIC nic) {
        if (nic.isSetNetwork() && !nic.getNetwork().isSetId() && nic.getNetwork().isSetName()) {
            Guid clusterId = getClusterId();
            org.ovirt.engine.core.common.businessentities.network.Network network = lookupClusterNetwork(clusterId, nic.getNetwork().getId()==null ? null : asGuid(nic.getNetwork().getId()), nic.getNetwork().getName());
            if (network!=null) {
                nic.getNetwork().setName(null);
                nic.getNetwork().setId(network.getId().toString());
            }
        }
    }

    protected abstract VmNetworkInterface setNetwork(NIC device, VmNetworkInterface ni);
}
