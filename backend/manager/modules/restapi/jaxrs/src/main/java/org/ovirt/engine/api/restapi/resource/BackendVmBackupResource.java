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

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Backup;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.VmBackupDisksResource;
import org.ovirt.engine.api.resource.VmBackupResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;


public class BackendVmBackupResource
        extends AbstractBackendSubResource<Backup, org.ovirt.engine.core.common.businessentities.VmBackup>
        implements VmBackupResource {

    BackendVmBackupsResource parent;

    org.ovirt.engine.core.compat.Guid vmId;

    public BackendVmBackupResource(String vmBackupId, org.ovirt.engine.core.compat.Guid vmId, BackendVmBackupsResource parent) {
        super(vmBackupId, Backup.class, org.ovirt.engine.core.common.businessentities.VmBackup.class);
        this.vmId = vmId;
        this.parent = parent;
    }

    public BackendVmBackupsResource getParent() {
        return parent;
    }

    @Override
    public Backup get() {
        return addLinks(performGet(QueryType.GetVmBackupById, new IdQueryParameters(guid)));
    }

    @Override
    public Response doFinalize(Action action) {
        get();
        VmBackupParameters prms = new VmBackupParameters();
        VmBackup vmBackup = new VmBackup();
        vmBackup.setId(asGuid(id));
        vmBackup.setVmId(vmId);
        prms.setVmBackup(vmBackup);
        return performAction(ActionType.StopVmBackup, prms);
    }

    @Override
    public VmBackupDisksResource getDisksResource() {
        return inject(new BackendVmBackupDisksResource(this));
    }


    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }
}
