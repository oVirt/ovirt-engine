package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.api.resource.CdRomResource;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendCdRomResource extends BackendDeviceResource<Cdrom, Cdroms, VM> implements CdRomResource {

    private Guid vmId;

    public BackendCdRomResource(
            Guid vmId,
            Guid cdRomId,
            AbstractBackendReadOnlyDevicesResource<Cdrom, Cdroms, VM> collection,
            VdcActionType updateType,
            ParametersProvider<Cdrom, VM> updateParametersProvider,
            String[] requiredUpdateFields,
            String... subCollections) {
        super(
            Cdrom.class,
            VM.class,
            cdRomId,
            collection,
            updateType,
            updateParametersProvider,
            requiredUpdateFields,
            subCollections
        );
        this.vmId = vmId;
    }

    @Override
    public Cdrom update(Cdrom resource) {
        if (QueryHelper.hasCurrentConstraint(getUriInfo())) {
            validateParameters(resource, requiredUpdateFields);
            performAction(VdcActionType.ChangeDisk,
                          new ChangeDiskCommandParameters(getEntity(entityResolver, true).getId(),
                                                          resource.getFile().getId()));
            return resource;
        } else {
            return super.update(resource);
        }
    }

    @Override
    public Cdrom get() {
        if (QueryHelper.hasCurrentConstraint(getUriInfo())) {
            VM vm = collection.lookupEntity(guid);
            if (vm == null) {
                return notFound();
            }
            // change the iso path so the result of 'map' will contain current cd instead of the
            // persistent configuration
            vm.setIsoPath(vm.getCurrentCd());
            return addLinks(populate(map(vm), vm));
        } else {
            return super.get();
        }
    }

    @Override
    public Response remove() {
        VM vm = getEntity(VM.class, VdcQueryType.GetVmByVmId, new IdQueryParameters(vmId), vmId.toString());
        VmStatic vmStatic = vm.getStaticData();
        vmStatic.setIsoPath(null);
        VmManagementParametersBase parameters = new VmManagementParametersBase(vmStatic);
        return performAction(VdcActionType.UpdateVm, parameters);
    }
}
