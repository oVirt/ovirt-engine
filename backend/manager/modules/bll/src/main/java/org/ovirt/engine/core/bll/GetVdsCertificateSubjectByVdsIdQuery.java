package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVdsCertificateSubjectByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsCertificateSubjectByVdsIdQuery(P parameters) {
        super(parameters);
    }

    public GetVdsCertificateSubjectByVdsIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setSucceeded(false);
        VDS vds = getDbFacade()
                .getVdsDao()
                .get(getParameters().getId(), getUserID(), getParameters().isFiltered());
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
        return Config.<String> getValue(ConfigValues.OrganizationName);
    }
}
