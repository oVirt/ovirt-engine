/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.NetworkFilterParameter;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.NicNetworkFilterParameterResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveVmNicFilterParameterParameters;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicFilterParameterResource
        extends AbstractBackendActionableResource<NetworkFilterParameter, VmNicFilterParameter>
        implements NicNetworkFilterParameterResource {

    private Guid nicId;
    private Guid vmId;

    public BackendVmNicFilterParameterResource(Guid vmId, Guid nicId, String vmNicFilterParameterId) {
        super(vmNicFilterParameterId, NetworkFilterParameter.class, VmNicFilterParameter.class);
        this.vmId = vmId;
        this.nicId = nicId;
    }

    @Override
    public NetworkFilterParameter get() {
        VmNicFilterParameter parameter = lookupParameter(guid);
        if (parameter != null) {
            return addLinks(populate(map(parameter), parameter));
        }
        return notFound();
    }

    @Override
    public NetworkFilterParameter addParents(NetworkFilterParameter parameter) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        Nic nic = new Nic();
        nic.setId(nicId.toString());
        nic.setVm(vm);
        parameter.setNic(nic);
        return parameter;
    }

    private VmNicFilterParameter lookupParameter(Guid parameterId) {
        List<VmNicFilterParameter> parameters = getBackendCollection(
            VmNicFilterParameter.class,
            QueryType.GetVmInterfaceFilterParameterById,
            new IdQueryParameters(guid)
        );
        for (VmNicFilterParameter parameter : parameters) {
            if (Objects.equals(parameter.getId(), parameterId)) {
                return parameter;
            }
        }
        return null;
    }

    @Override
    public NetworkFilterParameter update(NetworkFilterParameter parameter) {
        return performUpdate(
                parameter,
                new FilterParameterResolver(),
                ActionType.UpdateVmNicFilterParameterLive,
                new UpdateParametersProvider()
        );
    }

    @Override
    public Response remove() {
        return performAction(
                ActionType.RemoveVmNicFilterParameterLive,
                new RemoveVmNicFilterParameterParameters(vmId, guid)
        );
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    private class FilterParameterResolver extends EntityIdResolver<Guid> {
        @Override
        public VmNicFilterParameter lookupEntity(Guid paramId) throws BackendFailureException {
            return lookupParameter(paramId);
        }
    }

    private class UpdateParametersProvider
            implements ParametersProvider<NetworkFilterParameter, VmNicFilterParameter> {
        @Override
        public ActionParametersBase getParameters(NetworkFilterParameter incoming, VmNicFilterParameter entity) {
            VmNicFilterParameter parameter = map(incoming, entity);
            parameter.setVmInterfaceId(nicId);
            return new VmNicFilterParameterParameters(vmId, parameter);
        }
    }
}
