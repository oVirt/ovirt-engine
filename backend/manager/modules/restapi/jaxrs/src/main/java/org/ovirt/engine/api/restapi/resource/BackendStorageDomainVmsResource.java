package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VMs;
import org.ovirt.engine.api.resource.StorageDomainContentResource;
import org.ovirt.engine.api.resource.StorageDomainContentsResource;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainVmsResource
    extends AbstractBackendStorageDomainContentsResource<VMs, VM, org.ovirt.engine.core.common.businessentities.VM>
        implements StorageDomainContentsResource<VMs, VM> {

    static final String[] SUB_COLLECTIONS = { "disks" };

    public BackendStorageDomainVmsResource(Guid storageDomainId) {
        super(storageDomainId, VM.class, org.ovirt.engine.core.common.businessentities.VM.class, SUB_COLLECTIONS);
    }

    @Override
    public VMs list() {
        VMs vms = new VMs();
        if (QueryHelper.hasMatrixParam(getUriInfo(), UNREGISTERED_CONSTRAINT_PARAMETER)) {
            List<org.ovirt.engine.core.common.businessentities.VM> unregisteredVms =
                    getBackendCollection(VdcQueryType.GetUnregisteredVms,
                            new IdQueryParameters(storageDomainId));
            List<VM> collection = new ArrayList<VM>();
            for (org.ovirt.engine.core.common.businessentities.VM entity : unregisteredVms) {
                VM vm = map(entity);
                collection.add(addLinks(populate(vm, entity)));
            }
            vms.getVMs().addAll(collection);
        } else {
            vms.getVMs().addAll(getCollection());
        }
        return vms;
    }

    @Override
    protected VM addParents(VM vm) {
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
    @SingleEntityResource
    public StorageDomainContentResource<VM> getStorageDomainContentSubResource(String id) {
        return inject(new BackendStorageDomainVmResource(this, id));
    }

    @Override
    protected VM doPopulate(VM model, org.ovirt.engine.core.common.businessentities.VM entity) {
        return model;
    }
}
