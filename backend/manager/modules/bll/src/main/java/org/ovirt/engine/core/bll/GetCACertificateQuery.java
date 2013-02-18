package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.FileUtil;

public class GetCACertificateQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetCACertificateQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setSucceeded(false);
        String path = Config.resolveCACertificatePath();
        if (FileUtil.fileExists(path)) {
            getQueryReturnValue().setReturnValue(FileUtil.readAllText(path));
            getQueryReturnValue().setSucceeded(true);
        }
    }
}
