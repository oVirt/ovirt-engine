package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetVdsCertificateSubjectByVdsIdQuery<P extends GetVdsByVdsIdParameters> extends QueriesCommandBase<P> {
    public GetVdsCertificateSubjectByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setSucceeded(false);
        VDS vds = DbFacade.getInstance().getVdsDAO().get(getParameters().getVdsId());
        if (vds != null) {
            getQueryReturnValue().setSucceeded(true);
            getQueryReturnValue()
                    .setReturnValue(
                            String.format("O=%1$s,CN=%2$s", Config.<String> GetValue(ConfigValues.OrganizationName)
                                    .replace("\\", "\\\\").replace(",", "\\,"), vds.gethost_name()
                                    .replace("\\", "\\\\").replace(",", "\\,")));
        }
    }
}
