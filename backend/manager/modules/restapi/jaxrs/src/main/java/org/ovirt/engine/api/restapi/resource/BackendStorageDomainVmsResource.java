package org.ovirt.engine.api.restapi.resource;

import java.util.Collection;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VMs;
import org.ovirt.engine.api.resource.RemovableStorageDomainContentsResource;
import org.ovirt.engine.api.resource.StorageDomainContentResource;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParamenters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryTopSizeVmsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainVmsResource
    extends AbstractBackendStorageDomainContentsResource<VMs, VM, org.ovirt.engine.core.common.businessentities.VM>
    implements RemovableStorageDomainContentsResource<VMs, VM> {

    public BackendStorageDomainVmsResource(Guid storageDomainId) {
        super(storageDomainId, VM.class, org.ovirt.engine.core.common.businessentities.VM.class);
    }

    @Override
    public VMs list() {
        VMs vms = new VMs();
        vms.getVMs().addAll(getCollection());
        return vms;
    }

    @Override
    protected VM addParents(VM vm) {
        vm.setStorageDomain(getStorageDomainModel());
        return vm;
    }

    @Override
    protected Collection<org.ovirt.engine.core.common.businessentities.VM> getEntitiesFromDataDomain() {
        return getBackendCollection(VdcQueryType.GetTopSizeVmsFromStorageDomain,
                                    new StorageDomainQueryTopSizeVmsParameters(storageDomainId, -1));
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
    public Response performRemove(String id) {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        vm.setId(Guid.createGuidFromString(id));
        RemoveVmFromImportExportParamenters params = new RemoveVmFromImportExportParamenters(
                vm,
                storageDomainId,
                getDataCenterId(storageDomainId));
        return performAction(VdcActionType.RemoveVmFromImportExport, params);
    }
}
