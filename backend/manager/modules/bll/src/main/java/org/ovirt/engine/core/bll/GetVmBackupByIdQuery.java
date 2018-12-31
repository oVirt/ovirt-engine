package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmBackupDao;

public class GetVmBackupByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmBackupDao vmBackupDao;

    public GetVmBackupByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VmBackup vmBackup = vmBackupDao.get(getParameters().getId());
        if (vmBackup != null) {
            vmBackup.setDisks(vmBackupDao.getDisksByBackupId(vmBackup.getId()));
        }
        setReturnValue(vmBackup);
    }
}
