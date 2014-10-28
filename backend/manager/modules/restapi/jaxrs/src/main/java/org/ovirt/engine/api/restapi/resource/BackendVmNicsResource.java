package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.api.model.ReportedDevices;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.VmNicResource;
import org.ovirt.engine.api.resource.VmNicsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.api.restapi.types.ReportedDeviceMapper;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicsResource extends BackendNicsResource implements VmNicsResource {


    public BackendVmNicsResource(Guid parentId) {
        super(parentId,
              VdcQueryType.GetVmInterfacesByVmId,
              new IdQueryParameters(parentId),
              VdcActionType.AddVmInterface,
              VdcActionType.RemoveVmInterface,
              VdcActionType.UpdateVmInterface);
    }

    @Override
    protected ParametersProvider<NIC, VmNetworkInterface> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    protected class UpdateParametersProvider implements ParametersProvider<NIC, VmNetworkInterface> {
        @Override
        public VdcActionParametersBase getParameters(NIC incoming, VmNetworkInterface entity) {
            VmNetworkInterface nic = map(incoming, entity);
            return new AddVmInterfaceParameters(parentId,
                    nic,
                    incoming.isSetNetwork() ? (incoming.getNetwork().isSetName() ? nic.getNetworkName() : "") : null,
                    incoming.isSetPortMirroring() ? nic.isPortMirroring() : false);
        }
    }

    @Override
    public Response add(NIC device) {
        //TODO: this is temporary mapping between engine boolean port mirroring parameter, and REST
        //      port mirroring network collection, next engine version will support the network collection
        //      in port mirroring

        validateEnums(NIC.class, device);
        boolean fault = false;
        String faultString = "The port mirroring network must match the Network set on the NIC";
        boolean isPortMirroring = device.isSetPortMirroring() && device.getPortMirroring().isSetNetworks();
        boolean isPortMirroringExceeded =
                isPortMirroring && device.getPortMirroring().getNetworks().getNetworks().size() > 1;
        if (!fault && isPortMirroringExceeded) {
            fault = true;
            faultString = "Cannot set more than one network in port mirroring mode";
        }
        isPortMirroring = isPortMirroring && device.getPortMirroring().getNetworks().getNetworks().size() == 1;
        if (!fault && isPortMirroring) {
            org.ovirt.engine.api.model.Network pmNetwork = device.getPortMirroring().getNetworks().getNetworks().get(0);
            String pmNetworkId = (pmNetwork.isSetId() ? pmNetwork.getId() : null);
            String pmNetworkName = (pmNetwork.isSetName() ? pmNetwork.getName() : null);
            String networkId =
                    (device.isSetNetwork() && device.getNetwork().isSetId()) ? device.getNetwork().getId() : null;
            String networkName =
                    (device.isSetNetwork() && device.getNetwork().isSetName()) ? device.getNetwork().getName() : null;

            if (networkName != null || networkId != null) {
                if (pmNetworkId != null) {
                    networkId = (networkId == null) ? getNetworkId(networkName) : networkId;
                    fault = (!networkId.equals(pmNetworkId));
                } else if (pmNetworkName != null) {
                    if (networkName == null && networkId != null) {
                        pmNetworkId = getNetworkId(pmNetworkName);
                        fault = (!networkId.equals(pmNetworkId));
                    }
                    fault = fault || (!pmNetworkName.equals(networkName));
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
        return super.add(device);
    }

    private String getNetworkId(String networkName) {
        if (networkName != null) {
            Guid clusterId = getClusterId();
            org.ovirt.engine.core.common.businessentities.network.Network n = getClusterNetwork(clusterId, null, networkName);
            if (n != null) {
                return n.getId().toString();
            }
        }
        return null;
    }

    @Override
    protected NIC doPopulate(NIC model, VmNetworkInterface entity) {
        return model;
    }

    @Override
    protected NIC deprecatedPopulate(NIC model, VmNetworkInterface entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        addReportedDevices(model, entity);
        if (details.contains("statistics")) {
            addStatistics(model, entity);
        }
        return model;
    }

    public void addStatistics(NIC model, VmNetworkInterface entity) {
        model.setStatistics(new Statistics());
        NicStatisticalQuery query = new NicStatisticalQuery(newModel(model.getId()));
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(uriInfo, statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    void addReportedDevices(NIC model, VmNetworkInterface entity) {
        List<ReportedDevice> devices = getDevices(entity.getVmId(), entity.getMacAddress());
        if (!devices.isEmpty()) {
            ReportedDevices reportedDevices = new ReportedDevices();
            reportedDevices.getReportedDevices().addAll(devices);
            model.setReportedDevices(reportedDevices);
        }
    }

    public List<ReportedDevice> getDevices(Guid vmId, String mac) {
        List<ReportedDevice> devices = new ArrayList<ReportedDevice>();
        for (VmGuestAgentInterface iface : getDevicesCollection(vmId)) {
            if (StringUtils.equals(iface.getMacAddress(), mac)) {
                devices.add(LinkHelper.addLinks(getUriInfo(), ReportedDeviceMapper.map(iface, new ReportedDevice())));
            }
        }
        return devices;
    }

    private List<VmGuestAgentInterface> getDevicesCollection(Guid vmId) {
        return getBackendCollection(VmGuestAgentInterface.class,
                VdcQueryType.GetVmGuestAgentInterfacesByVmId,
                new IdQueryParameters(vmId));
    }

    @Override
    protected VdcActionParametersBase getAddParameters(VmNetworkInterface entity, NIC nic) {
        return new AddVmInterfaceParameters(parentId,
                setNetwork(nic, entity),
                nic.isSetNetwork() ? entity.getNetworkName() : null,
                nic.isSetPortMirroring() ? entity.isPortMirroring() : false);
    }

    @Override
    protected VdcActionParametersBase getRemoveParameters(String id) {
        return new RemoveVmInterfaceParameters(parentId, asGuid(id));
    }

    @Override
    @SingleEntityResource
    public VmNicResource getDeviceSubResource(String id) {
        return inject(new BackendVmNicResource(id,
                                             this,
                                             updateType,
                                             getUpdateParametersProvider(),
                                             getRequiredUpdateFields(),
                                             subCollections));
    }

    @Override
    protected VmNetworkInterface setNetwork(NIC device, VmNetworkInterface ni) {
        if (device.isSetNetwork()) {
            if (device.getNetwork().isSetId() || device.getNetwork().isSetName()) {
                Guid clusterId = getClusterId();
                Network net =
                        lookupClusterNetwork(clusterId, device.getNetwork().isSetId() ? asGuid(device.getNetwork()
                                .getId()) : null, device.getNetwork().getName());
                if (net != null) {
                    ni.setNetworkName(net.getName());
                }
            } else {
                ni.setNetworkName("");
            }
        }
        return ni;
    }

    @Override
    protected Guid getClusterId() {
        Guid clusterId = getEntity(VM.class,
            VdcQueryType.GetVmByVmId,
            new IdQueryParameters(parentId), "id").getVdsGroupId();
        return clusterId;
    }
}
