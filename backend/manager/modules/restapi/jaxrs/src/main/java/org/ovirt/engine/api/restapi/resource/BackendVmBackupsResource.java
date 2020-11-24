/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Backup;
import org.ovirt.engine.api.model.Backups;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmBackupResource;
import org.ovirt.engine.api.resource.VmBackupsResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmBackupsResource
        extends AbstractBackendCollectionResource<Backup, org.ovirt.engine.core.common.businessentities.VmBackup>
        implements VmBackupsResource {

    protected static final String REQUIRE_CONSISTENCY_CONSTRAINT_PARAMETER = "require_consistency";

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
        boolean requireConsistency = ParametersHelper.getBooleanParameter(
                httpHeaders, uriInfo, REQUIRE_CONSISTENCY_CONSTRAINT_PARAMETER, true, false);
        entity.setVmId(vmId);
        return performCreate(ActionType.StartVmBackup,
                new VmBackupParameters(entity, requireConsistency),
                new QueryIdResolver<Guid>(QueryType.GetVmBackupById, IdQueryParameters.class));
    }

    @Override
    public VmBackupResource getBackupResource(String id) {
        return inject(new BackendVmBackupResource(id, vmId, this));
    }
}
