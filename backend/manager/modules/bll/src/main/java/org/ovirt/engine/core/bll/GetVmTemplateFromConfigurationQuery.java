package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.queries.GetVmFromConfigurationQueryParameters;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetVmTemplateFromConfigurationQuery<P extends GetVmFromConfigurationQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private OvfHelper ovfHelper;

    public GetVmTemplateFromConfigurationQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }
    @Override
    protected void executeQueryCommand() {
        if (getParameters().getConfigurationType() == null) {
            log.error("received invalid configuration type: null");
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString("received invalid configuration type: null");
            return;
        }

        switch(getParameters().getConfigurationType()) {
        case OVF:
            try {
                getQueryReturnValue().setReturnValue(ovfHelper.readVmTemplateFromOvf(getParameters().getVmConfiguration()).getVm());
            } catch (OvfReaderException e) {
                log.warn("failed to parse a given ovf configuration: \n" + getParameters().getVmConfiguration(), e);
                getQueryReturnValue().setSucceeded(false);
                getQueryReturnValue().setExceptionString("failed to parse a given ovf configuration " + e.getMessage());
            }
            break;

        case OVA:
            try {
                getQueryReturnValue().setReturnValue(ovfHelper.readVmTemplateFromOva(getParameters().getVmConfiguration()));
            } catch (OvfReaderException e) {
                log.warn("failed to parse a given ovf configuration: \n" + getParameters().getVmConfiguration(), e);
                getQueryReturnValue().setSucceeded(false);
                getQueryReturnValue().setExceptionString("failed to parse a given ovf configuration " + e.getMessage());
            }
            break;
        }
    }
}
