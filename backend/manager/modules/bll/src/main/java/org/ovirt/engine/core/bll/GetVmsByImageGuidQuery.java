package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmsByImageGuidParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * A query to retrieve all the VMs connected to a given image.
 * The return value if a map from the image's plug state (<code>true</code>/<code>false</code>) to a {@link List} of the relevant VMs.
 */
public class GetVmsByImageGuidQuery<P extends GetVmsByImageGuidParameters> extends QueriesCommandBase<P> {
    public GetVmsByImageGuidQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getVmDAO()
                .getForImage(getParameters().getImageGuid()));
    }
}
