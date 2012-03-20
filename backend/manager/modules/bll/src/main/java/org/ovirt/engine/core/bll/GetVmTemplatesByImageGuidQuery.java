package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmTemplatesByImageGuidParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getVmTemplateDAO()
                .getAllForImage(getParameters().getImageGuid()));
    }
}
