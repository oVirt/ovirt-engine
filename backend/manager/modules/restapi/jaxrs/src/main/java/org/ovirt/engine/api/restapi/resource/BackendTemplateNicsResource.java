package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateNicsResource
            extends BackendNicsResource
            implements DevicesResource<NIC, Nics>{

    public BackendTemplateNicsResource(Guid parentId) {
        super(parentId,
              VdcQueryType.GetTemplateInterfacesByTemplateId,
              new IdQueryParameters(parentId),
              VdcActionType.AddVmTemplateInterface,
              VdcActionType.UpdateVmTemplateInterface);
    }

    @Override
    protected ParametersProvider<NIC, VmNetworkInterface> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    protected class UpdateParametersProvider implements ParametersProvider<NIC, VmNetworkInterface> {
        @Override
        public VdcActionParametersBase getParameters(NIC incoming, VmNetworkInterface entity) {
            VmNetworkInterface nic = map(incoming, entity);
            return new AddVmTemplateInterfaceParameters(parentId,
                    nic,
                    incoming.isSetNetwork() ? (incoming.getNetwork().isSetName() ? nic.getNetworkName() : "") : null,
                    incoming.isSetPortMirroring() ? nic.isPortMirroring() : false);
        }
    }

    @Override
    protected VdcActionParametersBase getAddParameters(VmNetworkInterface entity, NIC nic) {
        VmNetworkInterface iface = setNetwork(nic, entity);
        return new AddVmTemplateInterfaceParameters(parentId,
                iface,
                nic.isSetNetwork() ? iface.getNetworkName() : null,
                nic.isSetPortMirroring() ? iface.isPortMirroring() : false);
    }

    @Override
    @SingleEntityResource
    public DeviceResource<NIC> getDeviceSubResource(String id) {
        return inject(
            new BackendTemplateNicResource(
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

    @Override
    protected VmNetworkInterface setNetwork(NIC device, VmNetworkInterface ni) {
        if (device.isSetNetwork()) {
            if (device.getNetwork().isSetId() || device.getNetwork().isSetName()) {
                Guid clusterId = getEntity(VmTemplate.class,
                        VdcQueryType.GetVmTemplate,
                        new GetVmTemplateParameters(parentId), "id").getVdsGroupId();
                Network net =
                        lookupClusterNetwork(clusterId, device.getNetwork().isSetId() ? asGuid(device.getNetwork()
                                .getId())
                                : null, device.getNetwork().getName());
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
    public NIC addParents(NIC device) {
        device.setTemplate(new Template());
        device.getTemplate().setId(parentId.toString());
        return device;
    }

    @Override
    protected Guid getClusterId() {
        Guid clusterId = getEntity(VmTemplate.class,
                                   VdcQueryType.GetVmTemplate,
                                   new GetVmTemplateParameters(parentId), "id").getVdsGroupId();
        return clusterId;
    }
}
