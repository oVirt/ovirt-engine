package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;

/**
 * A query to retrieve all the VMs connected to a given image.
 * The return value if a map from the image's plug state (<code>true</code>/<code>false</code>) to a {@link List} of the relevant VMs.
 */
public class GetVmsByDiskGuidQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDao vmDao;

    public GetVmsByDiskGuidQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmDao.getForDisk(getParameters().getId(), true));
    }
}
