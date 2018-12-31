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

import org.ovirt.engine.api.model.Backup;
import org.ovirt.engine.api.model.Backups;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmBackupResource;
import org.ovirt.engine.api.resource.VmBackupsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmBackupsResource
        extends AbstractBackendCollectionResource<Backup, org.ovirt.engine.core.common.businessentities.VmBackup>
        implements VmBackupsResource {

    private org.ovirt.engine.core.compat.Guid vmId;

    public BackendVmBackupsResource(org.ovirt.engine.core.compat.Guid vmId) {
        super(org.ovirt.engine.api.model.Backup.class, org.ovirt.engine.core.common.businessentities.VmBackup.class);
        this.vmId = vmId;
    }

    @Override
    public Backup addParents(Backup backup) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        backup.setVm(vm);
        return backup;
    }

    @Override
    public Backups list() {
        return mapCollection(getBackendCollection(QueryType.GetAllVmBackupsByVmId,
                new org.ovirt.engine.core.common.queries.IdQueryParameters(vmId)));
    }

    private org.ovirt.engine.api.model.Backups mapCollection(java.util.List<org.ovirt.engine.core.common.businessentities.VmBackup> entities) {
        Backups collection = new Backups();
        for (org.ovirt.engine.core.common.businessentities.VmBackup entity : entities) {
            collection.getBackups().add(addLinks(map(entity), Vm.class));
        }
        return collection;
    }

    public javax.ws.rs.core.Response add(org.ovirt.engine.api.model.Backup vmBackup) {
        org.ovirt.engine.core.common.businessentities.VmBackup entity = map(vmBackup);
        entity.setVmId(vmId);
        return performCreate(ActionType.StartVmBackup,
                new VmBackupParameters(entity),
                new QueryIdResolver<Guid>(QueryType.GetVmBackupById, IdQueryParameters.class));
    }

    @Override
    public VmBackupResource getBackupResource(String id) {
        return inject(new BackendVmBackupResource(id, vmId, this));
    }
}
