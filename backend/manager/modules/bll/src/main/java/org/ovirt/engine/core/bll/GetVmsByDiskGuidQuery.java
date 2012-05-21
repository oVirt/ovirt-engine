package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmsByDiskGuidParameters;

/**
 * A query to retrieve all the VMs connected to a given image.
 * The return value if a map from the image's plug state (<code>true</code>/<code>false</code>) to a {@link List} of the relevant VMs.
 */
public class GetVmsByDiskGuidQuery<P extends GetVmsByDiskGuidParameters> extends QueriesCommandBase<P> {
    public GetVmsByDiskGuidQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getVmDAO()
                .getForDisk(getParameters().getDiskGuid()));
    }
}
