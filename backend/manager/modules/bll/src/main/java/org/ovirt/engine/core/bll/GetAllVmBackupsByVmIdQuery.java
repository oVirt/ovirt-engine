package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmBackupDao;

public class GetAllVmBackupsByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmBackupDao vmBackupDao;

    public GetAllVmBackupsByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmBackup> vmBackups = vmBackupDao.getAllForVm(getParameters().getId());
        vmBackups.forEach(vmBackup -> vmBackup.setDisks(vmBackupDao.getDisksByBackupId(vmBackup.getId())));
        setReturnValue(vmBackups);
    }
}
