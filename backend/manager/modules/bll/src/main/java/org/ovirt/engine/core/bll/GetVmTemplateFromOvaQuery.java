package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmFromOvaQueryParameters;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetVmTemplateFromOvaQuery<T extends GetVmFromOvaQueryParameters> extends GetFromOvaQuery<VmTemplate, T> {

    @Inject
    private OvfHelper ovfHelper;

    public GetVmTemplateFromOvaQuery(T parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected VmTemplate parseOvf(String ovf) {
        if (!ovf.contains("xmlns:ovirt")) {
            log.error("Only oVirt OVA can be imported as a template, got:\n {}", ovf);
            getQueryReturnValue().setExceptionString("found invalid template");
            return null;
        }

        try {
            return readVmTemplateFromOva(ovf);
        } catch (OvfReaderException e) {
            log.debug("failed to parse a given ovf configuration: \n" + ovf, e);
            getQueryReturnValue().setExceptionString("failed to parse a given ovf configuration " + e.getMessage());
            return null;
        }
    }

    private VmTemplate readVmTemplateFromOva(String ovf) throws OvfReaderException {
        return ovf != null ? ovfHelper.readVmTemplateFromOva(ovf) : null;
    }

    @Override
    protected VmEntityType getEntityType() {
        return VmEntityType.TEMPLATE;
    }
}
