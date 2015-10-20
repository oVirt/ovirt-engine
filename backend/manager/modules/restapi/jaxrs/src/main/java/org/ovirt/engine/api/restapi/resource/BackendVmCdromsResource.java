/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmCdromResource;
import org.ovirt.engine.api.resource.VmCdromsResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmCdromsResource
        extends AbstractBackendCollectionResource<Cdrom, VM>
        implements VmCdromsResource {

    private Guid vmId;

    public BackendVmCdromsResource(Guid vmId) {
        super(Cdrom.class, VM.class);
        this.vmId = vmId;
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

    @Override
    public Response add(Cdrom cdrom) {
        validateParameters(cdrom, "file.id");
        VM vm = getVm();
        vm = map(cdrom, vm);
        VmManagementParametersBase parameters = new VmManagementParametersBase(vm);
        return performCreate(VdcActionType.UpdateVm, parameters, new VmResolver());
    }

    @Override
    public Cdrom addParents(Cdrom entity) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        entity.setVm(vm);
        return entity;
    }

    private VM getVm() {
        return getEntity(
            VM.class,
            VdcQueryType.GetVmByVmId,
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
