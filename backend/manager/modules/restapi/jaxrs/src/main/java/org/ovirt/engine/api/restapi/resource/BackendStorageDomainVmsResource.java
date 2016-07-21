package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.StorageDomainVmResource;
import org.ovirt.engine.api.resource.StorageDomainVmsResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainVmsResource
        extends AbstractBackendStorageDomainContentsResource<Vms, Vm, org.ovirt.engine.core.common.businessentities.VM>
        implements StorageDomainVmsResource {

    static final String[] SUB_COLLECTIONS = { "disks" };

    public BackendStorageDomainVmsResource(Guid storageDomainId) {
        super(storageDomainId, Vm.class, org.ovirt.engine.core.common.businessentities.VM.class, SUB_COLLECTIONS);
    }

    @Override
    public Vms list() {
        Vms vms = new Vms();
        boolean unregistered = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, UNREGISTERED_CONSTRAINT_PARAMETER, true, false);
        if (unregistered) {
            List<org.ovirt.engine.core.common.businessentities.VM> unregisteredVms =
                    getBackendCollection(VdcQueryType.GetUnregisteredVms,
                            new IdQueryParameters(storageDomainId));
            List<Vm> collection = new ArrayList<>();
            for (org.ovirt.engine.core.common.businessentities.VM entity : unregisteredVms) {
                Vm vm = map(entity);
                collection.add(addLinks(populate(vm, entity)));
            }
            vms.getVms().addAll(collection);
        } else {
            vms.getVms().addAll(getCollection());
        }
        return vms;
    }

    @Override
    protected Vm addParents(Vm vm) {
        vm.setStorageDomain(getStorageDomainModel());
        return vm;
    }

    @Override
    protected Collection<org.ovirt.engine.core.common.businessentities.VM> getEntitiesFromExportDomain() {
        GetAllFromExportDomainQueryParameters params =
            new GetAllFromExportDomainQueryParameters(getDataCenterId(storageDomainId), storageDomainId);

        return getBackendCollection(VdcQueryType.GetVmsFromExportDomain, params);
    }

    @Override
    public StorageDomainVmResource getVmResource(String id) {
        return inject(new BackendStorageDomainVmResource(this, id));
    }
}
