/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.NicNetworkFilterParametersResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VmNicResource;
import org.ovirt.engine.api.resource.VmReportedDevicesResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicResource
        extends AbstractBackendActionableResource<Nic, VmNetworkInterface>
        implements VmNicResource {

    private Guid vmId;

    public BackendVmNicResource(String nicId, Guid vmId) {
        super(nicId, Nic.class, VmNetworkInterface.class);
        this.vmId = vmId;
    }

    @Override
    public Nic get() {
        VmNetworkInterface nic = lookupNic(guid);
        if (nic != null) {
            return addLinks(populate(map(nic), nic));
        }
        return notFound();
    }

    private VmNetworkInterface lookupNic(Guid nicId) {
        List<VmNetworkInterface> nics = getBackendCollection(
            VmNetworkInterface.class,
            QueryType.GetVmInterfacesByVmId,
            new IdQueryParameters(vmId)
        );
        for (VmNetworkInterface nic : nics) {
            if (Objects.equals(nic.getId(), nicId)) {
                return nic;
            }
        }
        return null;
    }

    @Override
    public Nic update(Nic nic) {
        return performUpdate(
                nic,
                new NicResolver(),
                ActionType.UpdateVmInterface,
                new UpdateParametersProvider()
        );
    }

    @Override
    public Response activate(Action action) {
        Nic nic = get();
        nic.setPlugged(true);
        update(nic);
        return actionSuccess(action);
    }

    @Override
    public Response deactivate(Action action) {
        Nic nic = get();
        nic.setPlugged(false);
        update(nic);
        return actionSuccess(action);
    }

    @Override
    public VmReportedDevicesResource getReportedDevicesResource() {
        return inject(new BackendVmReportedDevicesResource(vmId));
    }

    @Override
    public NicNetworkFilterParametersResource getNetworkFilterParametersResource() {
        return inject(new BackendVmNicFilterParametersResource(vmId, guid));
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveVmInterface, new RemoveVmInterfaceParameters(vmId, guid));
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    @Override
    public CreationResource getCreationResource(String oid) {
        return inject(new BackendCreationResource(oid));
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        NicStatisticalQuery query = new NicStatisticalQuery(new NicResolver(), newModel(id));
        return inject(new BackendStatisticsResource<>(entityType, guid, query));
    }

    @Override
    protected Nic addParents(Nic nic) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        nic.setVm(vm);
        return nic;
    }

    private class NicResolver extends EntityIdResolver<Guid> {
        @Override
        public VmNetworkInterface lookupEntity(Guid nicId) throws BackendFailureException {
            return lookupNic(nicId);
        }
    }

    private class UpdateParametersProvider implements ParametersProvider<Nic, VmNetworkInterface> {
        @Override
        public ActionParametersBase getParameters(Nic incoming, VmNetworkInterface entity) {
            VmNetworkInterface nic = map(incoming, entity);
            return new AddVmInterfaceParameters(vmId, nic);
        }
    }
}
