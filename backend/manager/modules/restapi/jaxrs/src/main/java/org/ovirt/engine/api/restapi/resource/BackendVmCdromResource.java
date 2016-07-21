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
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.VmCdromResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmCdromResource
        extends AbstractBackendSubResource<Cdrom, VM>
        implements VmCdromResource {
    private static final String CURRENT = "current";

    private Guid vmId;

    public BackendVmCdromResource(String cdromId, Guid vmId) {
        super(cdromId, Cdrom.class, VM.class);
        this.vmId = vmId;
    }

    @Override
    public Cdrom get() {
        VM vm = getVm();
        boolean current = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, CURRENT, true, false);
        if (current) {
            // change the iso path so the result of 'map' will contain current cd instead of the
            // persistent configuration
            vm.setIsoPath(vm.getCurrentCd());
        }
        return addLinks(populate(map(vm), vm));
    }

    @Override
    public Cdrom update(Cdrom cdrom) {
        validateParameters(cdrom, "file");
        boolean current = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, CURRENT, true, false);
        if (current) {
            ChangeDiskCommandParameters parameters = new ChangeDiskCommandParameters(vmId, cdrom.getFile().getId());
            performAction(VdcActionType.ChangeDisk, parameters);
        }
        else {
            VM vm = getVm();
            vm = map(cdrom, vm);
            VmManagementParametersBase parameters = new VmManagementParametersBase(vm);
            performAction(VdcActionType.UpdateVm, parameters);
        }
        return get();
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

    @Override
    public Cdrom addParents(Cdrom entity) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        entity.setVm(vm);
        return entity;
    }

    @Override
    public Response remove() {
        VM vm = getVm();
        vm.setIsoPath(null);
        VmManagementParametersBase parameters = new VmManagementParametersBase(vm);
        return performAction(VdcActionType.UpdateVm, parameters);
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }
}
