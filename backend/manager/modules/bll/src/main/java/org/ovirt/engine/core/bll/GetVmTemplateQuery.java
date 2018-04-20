package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetVmTemplateQuery<P extends GetVmTemplateParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmHandler vmHandler;

    @Inject
    private VmTemplateHandler vmTemplateHandler;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private ClusterDao clusterDao;

    public GetVmTemplateQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VmTemplate vmt;
        GetVmTemplateParameters params = getParameters();
        if (params.getName() != null) {
            Guid storagePoolId = getStoragePoolId(); // If no DC info available, the query will return the first
                                                     // Template with the given name found.
            vmt = vmTemplateDao.getByName(params.getName(), storagePoolId, getUserID(), params.isFiltered());
        } else {
            vmt = vmTemplateDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        }
        if (vmt != null) {
            vmTemplateHandler.updateDisksFromDb(vmt);
            vmHandler.updateVmInitFromDB(vmt, true);
        }
        getQueryReturnValue().setReturnValue(vmt);
    }

    // Get the datacenter ID.
    private Guid getStoragePoolId() {
        Guid result = null;
        GetVmTemplateParameters params = getParameters();
        if (params.getDataCenterId() != null) {
            result = params.getDataCenterId();
        } else if (params.getClusterId() != null) {
            Cluster cluster = clusterDao.get(params.getClusterId());
            if (cluster != null) {
                result = cluster.getStoragePoolId();
            }
        }
        return result;
    }
}
