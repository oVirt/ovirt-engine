package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.NetworkFilterParameter;
import org.ovirt.engine.api.model.NetworkFilterParameters;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.NicNetworkFilterParameterResource;
import org.ovirt.engine.api.resource.NicNetworkFilterParametersResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicFilterParametersResource
        extends AbstractBackendCollectionResource<NetworkFilterParameter, VmNicFilterParameter>
        implements NicNetworkFilterParametersResource {
    private Guid nicId;
    private Guid vmId;

    public BackendVmNicFilterParametersResource(Guid vmId, Guid nicId) {
        super(NetworkFilterParameter.class, VmNicFilterParameter.class);
        this.vmId = vmId;
        this.nicId = nicId;
    }

    @Override
    public NetworkFilterParameters list() {
        NetworkFilterParameters parameters = new NetworkFilterParameters();
        List<VmNicFilterParameter> entities = getBackendCollection(
            QueryType.GetVmInterfaceFilterParametersByVmInterfaceId,
            new IdQueryParameters(nicId)
        );
        for (VmNicFilterParameter entity : entities) {
            NetworkFilterParameter parameter = populate(map(entity), entity);
            parameters.getNetworkFilterParameters().add(addLinks(parameter));
        }
        return parameters;
    }

    @Override
    public Response add(NetworkFilterParameter parameter) {
        validateParameters(parameter, "value");
        VmNicFilterParameter vmNicFilterParameter = map(parameter);
        vmNicFilterParameter.setVmInterfaceId(nicId);
        return performCreate(
            ActionType.AddVmNicFilterParameterLive,
            new VmNicFilterParameterParameters(vmId, vmNicFilterParameter),
            new NicNetworkFilterParameterResolver(parameter.getName())
        );
    }

    @Override
    public NicNetworkFilterParameterResource getParameterResource(String parameterId) {
        return inject(new BackendVmNicFilterParameterResource(vmId, nicId, parameterId));
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

    private class NicNetworkFilterParameterResolver extends EntityIdResolver<Guid> {
        private String name;

        NicNetworkFilterParameterResolver(String name) {
            this.name = name;
        }

        private VmNicFilterParameter lookupEntity(Guid id, String name) {
            List<VmNicFilterParameter> parameters = getBackendCollection(
                    VmNicFilterParameter.class,
                    QueryType.GetVmInterfaceFilterParameterById,
                    new IdQueryParameters(id)
            );

            for (VmNicFilterParameter parameter : parameters) {
                if (id.equals(parameter.getId()) || name.equals(parameter.getName())) {
                    return parameter;
                }
            }
            return null;
        }

        @Override
        public VmNicFilterParameter lookupEntity(Guid id) throws BackendFailureException {
            return lookupEntity(id, name);
        }
    }
}
