package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.backendcompat.File;
import org.ovirt.engine.core.compat.backendcompat.StreamReaderCompat;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.queries.*;

public class GetCACertificateQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetCACertificateQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setSucceeded(false);
        if (File.Exists(Config.resolveCACertificatePath())) {
            // C# TO JAVA CONVERTER NOTE: The following 'using' block is
            // replaced by its Java equivalent:
            // using (StreamReaderCompat reader = new
            // StreamReaderCompat(Config.resolveCACertificatePath());
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
