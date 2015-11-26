package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.storage.domain.GetUnregisteredEntitiesQuery;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetUnregisteredVmTemplatesQuery<P extends IdQueryParameters> extends GetUnregisteredEntitiesQuery<P> {
    public GetUnregisteredVmTemplatesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected OvfHelper getOvfHelper() {
        return super.getOvfHelper();
    }

    @Override
    protected void executeQueryCommand() {
        List<OvfEntityData> entityList = getOvfEntityList(VmEntityType.TEMPLATE);
        List<VmTemplate> vmTemplates = new ArrayList<>();
        OvfHelper ovfHelper = getOvfHelper();
        for (OvfEntityData ovf : entityList) {
            try {
                vmTemplates.add(ovfHelper.readVmTemplateFromOvf(ovf.getOvfData()));
            } catch (OvfReaderException e) {
                log.debug("failed to parse a given ovf configuration: \n" + ovf.getOvfData(), e);
                getQueryReturnValue().setExceptionString("failed to parse a given ovf configuration "
                        + e.getMessage());
            }
        }
        getQueryReturnValue().setSucceeded(true);
        getQueryReturnValue().setReturnValue(vmTemplates);
    }
}
