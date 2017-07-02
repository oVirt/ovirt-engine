package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Application;
import org.ovirt.engine.api.model.Applications;
import org.ovirt.engine.api.resource.VmApplicationResource;
import org.ovirt.engine.api.resource.VmApplicationsResource;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmApplicationsResource
        extends AbstractBackendResource<Application, VM>
        implements VmApplicationsResource {

    Guid vmId;
    public BackendVmApplicationsResource(Guid vmId) {
        super(Application.class, VM.class);
        this.vmId = vmId;
    }

    @Override
    public Applications list() {
        VM vm = getEntity(entityType, QueryType.GetVmByVmId, new IdQueryParameters(vmId), vmId.toString(), true);
        Applications applications = new Applications();
        String appList = vm.getAppList();
        if (appList != null) {
            for (String appName : appList.split(",")) {
                if (!appName.isEmpty()) {
                    applications.getApplications().add(addLinks(map(appName)));
                }
            }
        }
        return applications;
    }

    private Application map(String appName) {
        return getMapper(String.class, Application.class).map(appName, null);
    }

    @Override
    protected Application addParents(Application model) {
        model.setVm(new org.ovirt.engine.api.model.Vm());
        model.getVm().setId(vmId.toString());
        return model;
    }

    @Override
    public VmApplicationResource getApplicationResource(String id) {
        return inject(new BackendVmApplicationResource(id, this));
    }
}
