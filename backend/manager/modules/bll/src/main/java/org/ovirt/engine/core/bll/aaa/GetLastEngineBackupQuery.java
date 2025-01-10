package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.common.queries.GetLastEngineBackupParameters;
import org.ovirt.engine.core.dao.EngineBackupLogDao;

public class GetLastEngineBackupQuery<P extends GetLastEngineBackupParameters> extends QueriesCommandBase<P> {

    @Inject
    private EngineBackupLogDao engineBackupLogDao;

    public GetLastEngineBackupQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        EngineBackupLog log = (EngineBackupLog) engineBackupLogDao.getLastSuccessfulEngineBackup(getParameters().getEngineBackupScope());
        getQueryReturnValue().setReturnValue(log);
    }
}
