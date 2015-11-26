package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.businessentities.ConfigurationType;
import org.ovirt.engine.core.common.queries.GetVmFromConfigurationQueryParameters;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetVmFromConfigurationQuery<P extends GetVmFromConfigurationQueryParameters> extends QueriesCommandBase<P> {

    public GetVmFromConfigurationQuery(P parameters) {
        super(parameters);
    }
    @Override
    protected void executeQueryCommand() {
        if (ConfigurationType.OVF.equals(getParameters().getConfigurationType())) {
            OvfHelper ovfHelper = new OvfHelper();
            try {
                getQueryReturnValue().setReturnValue(ovfHelper.readVmFromOvf(getParameters().getVmConfiguration()));
                getQueryReturnValue().setSucceeded(true);
            } catch (OvfReaderException e) {
                log.debug("failed to parse a given ovf configuration: \n" + getParameters().getVmConfiguration(), e);
                getQueryReturnValue().setExceptionString("failed to parse a given ovf configuration " + e.getMessage());
            }
        }
    }
}
