package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;

public class GetVdsCertificateSubjectByVdsIdQuery<P extends GetVdsByVdsIdParameters> extends QueriesCommandBase<P> {
    public GetVdsCertificateSubjectByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setSucceeded(false);
        VDS vds = getDbFacade()
                .getVdsDao()
                .get(getParameters().getVdsId(), getUserID(), getParameters().isFiltered());
        if (vds != null) {
            getQueryReturnValue().setSucceeded(true);
            getQueryReturnValue()
                    .setReturnValue(
                            String.format("O=%1$s,CN=%2$s", getOrganizationName()
                                    .replace("\\", "\\\\").replace(",", "\\,"), vds.getHostName()
                                    .replace("\\", "\\\\").replace(",", "\\,")));
        }
    }

    /**
     * @return The organization's name from {@link Config}
     */
    protected String getOrganizationName() {
        return Config.<String> GetValue(ConfigValues.OrganizationName);
    }
}
