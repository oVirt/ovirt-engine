/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.VmCdromResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
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
            performAction(ActionType.ChangeDisk, parameters);
        } else {
            VM vm = getVm();
            vm = map(cdrom, vm);
            VmManagementParametersBase parameters = new VmManagementParametersBase(vm);
            performAction(ActionType.UpdateVm, parameters);
        }
        return get();
    }

    private VM getVm() {
        return getEntity(
            VM.class,
            QueryType.GetVmByVmId,
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

    public Response remove() {
        VM vm = getVm();
        vm.setIsoPath(null);
        VmManagementParametersBase parameters = new VmManagementParametersBase(vm);
        return performAction(ActionType.UpdateVm, parameters);
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }
}
