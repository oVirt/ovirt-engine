package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateNicsResource
            extends BackendNicsResource
            implements DevicesResource<Nic, Nics>{

    public BackendTemplateNicsResource(Guid parentId) {
        super(parentId,
              VdcQueryType.GetTemplateInterfacesByTemplateId,
              new IdQueryParameters(parentId),
              VdcActionType.AddVmTemplateInterface,
              VdcActionType.UpdateVmTemplateInterface);
    }

    @Override
    protected ParametersProvider<Nic, VmNetworkInterface> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    protected class UpdateParametersProvider implements ParametersProvider<Nic, VmNetworkInterface> {
        @Override
        public VdcActionParametersBase getParameters(Nic incoming, VmNetworkInterface entity) {
            VmNetworkInterface nic = map(incoming, entity);
            return new AddVmTemplateInterfaceParameters(parentId, nic);
        }
    }

    @Override
    protected VdcActionParametersBase getAddParameters(VmNetworkInterface entity, Nic nic) {
        return new AddVmTemplateInterfaceParameters(parentId, entity);
    }

    @Override
    public DeviceResource<Nic> getDeviceResource(String id) {
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
    public Nic addParents(Nic device) {
        device.setTemplate(new Template());
        device.getTemplate().setId(parentId.toString());
        return device;
    }
}
