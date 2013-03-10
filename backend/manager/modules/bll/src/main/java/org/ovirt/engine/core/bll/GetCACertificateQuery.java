package org.ovirt.engine.core.bll;

import java.io.File;
import java.io.IOException;

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
        if (new File(path).exists()) {
            try {
                getQueryReturnValue().setReturnValue(FileUtil.readAllText(path));
            } catch (IOException e) {
                getQueryReturnValue().setExceptionString(e.getMessage());
                return;
            }
            getQueryReturnValue().setSucceeded(true);
        }
    }
}
