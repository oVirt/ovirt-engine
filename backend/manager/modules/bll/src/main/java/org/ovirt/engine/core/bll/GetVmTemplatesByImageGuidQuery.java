package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplatesByImageGuidParameters;

/**
 * A query to retrieve all the VM templates connected to a given image.
 * The return value if a map from the image's plug state (<code>true</code>/<code>false</code>) to a {@link List} of the relevant VM Templates.
 */
public class GetVmTemplatesByImageGuidQuery<P extends GetVmTemplatesByImageGuidParameters> extends QueriesCommandBase<P> {
    public GetVmTemplatesByImageGuidQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Map<Boolean, List<VmTemplate>> allTemplates =
                getDbFacade().getVmTemplateDAO().getAllForImage(getParameters().getImageGuid());

        for (List<VmTemplate> templates : allTemplates.values()) {
            for (VmTemplate t : templates) {
                updateDisksFromDb(t);
            }
        }

        getQueryReturnValue().setReturnValue(allTemplates);
    }

    protected void updateDisksFromDb(VmTemplate t) {
        VmTemplateHandler.UpdateDisksFromDb(t);
    }
}
