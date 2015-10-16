package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Nic;
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
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
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
              VdcActionType.UpdateVmInterface);
    }

    @Override
    protected ParametersProvider<Nic, VmNetworkInterface> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    protected class UpdateParametersProvider implements ParametersProvider<Nic, VmNetworkInterface> {
        @Override
        public VdcActionParametersBase getParameters(Nic incoming, VmNetworkInterface entity) {
            VmNetworkInterface nic = map(incoming, entity);
            return new AddVmInterfaceParameters(parentId, nic);
        }
    }

    @Override
    public Response add(Nic device) {
        validateEnums(Nic.class, device);
        return super.add(device);
    }

    @Override
    protected Nic deprecatedPopulate(Nic model, VmNetworkInterface entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        addReportedDevices(model, entity);
        if (details.contains("statistics")) {
            addStatistics(model, entity);
        }
        return model;
    }

    public void addStatistics(Nic model, VmNetworkInterface entity) {
        model.setStatistics(new Statistics());
        NicStatisticalQuery query = new NicStatisticalQuery(newModel(model.getId()));
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(uriInfo, statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    void addReportedDevices(Nic model, VmNetworkInterface entity) {
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
    protected VdcActionParametersBase getAddParameters(VmNetworkInterface entity, Nic nic) {
        return new AddVmInterfaceParameters(parentId, entity);
    }

    @Override
    public VmNicResource getDeviceResource(String id) {
        return inject(
            new BackendVmNicResource(
                parentId,
                id,
                this,
                updateType,
                getUpdateParametersProvider(),
                getRequiredUpdateFields(),
                subCollections
            )
        );
    }
}
