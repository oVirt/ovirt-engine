package org.ovirt.engine.core.bll;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * A query to retrieve all the VM templates connected to a given image.
 * The return value if a map from the image's plug state (<code>true</code>/<code>false</code>) to the relevant VM Templates.
 */
public class GetVmTemplatesByImageGuidQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmTemplatesByImageGuidQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Map<Boolean, VmTemplate> templateMap =
                getDbFacade().getVmTemplateDao().getAllForImage(getParameters().getId());

        if (!templateMap.values().isEmpty()) {
            updateDisksFromDb(templateMap.values().iterator().next());
        }
        getQueryReturnValue().setReturnValue(templateMap);
    }

    protected void updateDisksFromDb(VmTemplate t) {
        VmTemplateHandler.updateDisksFromDb(t);
    }
}
