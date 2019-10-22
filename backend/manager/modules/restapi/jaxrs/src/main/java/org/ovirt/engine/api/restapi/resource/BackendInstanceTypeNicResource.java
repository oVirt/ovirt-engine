/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.InstanceTypeNicResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeNicResource
        extends AbstractBackendActionableResource<Nic, VmNetworkInterface>
        implements InstanceTypeNicResource {

    private Guid instanceTypeId;

    protected BackendInstanceTypeNicResource(String nicId, Guid instanceTypeId) {
        super(nicId, Nic.class, VmNetworkInterface.class);
        this.instanceTypeId = instanceTypeId;
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
            QueryType.GetTemplateInterfacesByTemplateId,
            new IdQueryParameters(instanceTypeId)
        );
        for (VmNetworkInterface nic : nics) {
            if (Objects.equals(nic.getId(), guid)) {
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
            ActionType.UpdateVmTemplateInterface,
            new UpdateParametersProvider()
        );
    }

    @Override
    public Response remove() {
        get();
        return performAction(
            ActionType.RemoveVmTemplateInterface,
            new RemoveVmTemplateInterfaceParameters(instanceTypeId, guid)
        );
    }

    @Override
    public CreationResource getCreationResource(String oid) {
        return inject(new BackendCreationResource(oid));
    }

    @Override
    protected Nic addParents(Nic nic) {
        InstanceType instanceType = new InstanceType();
        instanceType.setId(instanceTypeId.toString());
        nic.setInstanceType(instanceType);
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
            return new AddVmTemplateInterfaceParameters(instanceTypeId, nic);
        }
    }

}
