package org.ovirt.engine.core.bll;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

/**
 * A query to retrieve all the VM templates connected to a given image.
 * The return value if a map from the image's plug state (<code>true</code>/<code>false</code>) to the relevant VM Templates.
 */
public class GetVmTemplatesByImageGuidQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmTemplateHandler vmTemplateHandler;

    public GetVmTemplatesByImageGuidQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Map<Boolean, VmTemplate> templateMap = vmTemplateDao.getAllForImage(getParameters().getId());

        if (!templateMap.values().isEmpty()) {
            updateDisksFromDb(templateMap.values().iterator().next());
        }
        getQueryReturnValue().setReturnValue(templateMap);
    }

    protected void updateDisksFromDb(VmTemplate t) {
        vmTemplateHandler.updateDisksFromDb(t);
    }
}
