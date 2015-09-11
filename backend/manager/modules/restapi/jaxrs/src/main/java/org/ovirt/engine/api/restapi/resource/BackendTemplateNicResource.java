package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.NicResource;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateNicResource extends AbstractBackendNicResource implements NicResource {

    private Guid templateId;

    protected BackendTemplateNicResource(
            Guid templateId,
            String nicId,
            AbstractBackendReadOnlyDevicesResource<NIC, Nics, VmNetworkInterface> collection,
            VdcActionType updateType,
            ParametersProvider<NIC, VmNetworkInterface> updateParametersProvider,
            String[] requiredUpdateFields,
            String... subCollections) {
        super(
            nicId,
            collection,
            updateType,
            updateParametersProvider,
            requiredUpdateFields,
            subCollections
        );
        this.templateId = templateId;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveVmTemplateInterface, new RemoveVmTemplateInterfaceParameters(templateId, guid));
    }
}
