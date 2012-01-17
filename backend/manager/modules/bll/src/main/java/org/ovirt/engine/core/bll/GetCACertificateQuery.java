package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.backendcompat.StreamReaderCompat;
import org.ovirt.engine.core.utils.FileUtil;

public class GetCACertificateQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetCACertificateQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setSucceeded(false);
        if (FileUtil.fileExists(Config.resolveCACertificatePath())) {
            StreamReaderCompat reader = new StreamReaderCompat(Config.resolveCACertificatePath());
            try {
                getQueryReturnValue().setReturnValue(reader.ReadToEnd());
                getQueryReturnValue().setSucceeded(true);
            } finally {
                reader.dispose();
            }
        }
    }
}
