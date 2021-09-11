/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmCdromResource;
import org.ovirt.engine.api.resource.VmCdromsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmCdromsResource
        extends AbstractBackendCollectionResource<Cdrom, VM>
        implements VmCdromsResource {

    private Guid vmId;
    private VM vm;

    public BackendVmCdromsResource(Guid vmId) {
        super(Cdrom.class, VM.class);
        this.vmId = vmId;
    }

    protected BackendVmCdromsResource(VM vm) {
        this(vm.getId());
        this.vm = vm;
    }

    @Override
    public Cdroms list() {
        VM vm = getVm();
        return mapCollection(vm);
    }

    private Cdroms mapCollection(VM vm) {
        Cdroms collection = new Cdroms();
        collection.getCdroms().add(addLinks(populate(map(vm), vm)));
        return collection;
    }

    public Response add(Cdrom cdrom) {
        validateParameters(cdrom, "file.id");
        VM vm = getVm();
        vm = map(cdrom, vm);
        VmManagementParametersBase parameters = new VmManagementParametersBase(vm);
        return performCreate(ActionType.UpdateVm, parameters, new VmResolver());
    }

    @Override
    public Cdrom addParents(Cdrom entity) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        entity.setVm(vm);
        return entity;
    }

    private VM getVm() {
        if (vm != null) {
            return vm;
        }
        return getEntity(
            VM.class,
            QueryType.GetVmByVmId,
            new IdQueryParameters(vmId),
            vmId.toString(),
            true
        );
    }

    private class VmResolver implements IResolver<Guid, VM> {
        @Override
        public VM resolve(Guid id) throws BackendFailureException {
            return getVm();
        }
    }

    @Override
    public VmCdromResource getCdromResource(String id) {
        return inject(new BackendVmCdromResource(id, vmId));
    }

}
