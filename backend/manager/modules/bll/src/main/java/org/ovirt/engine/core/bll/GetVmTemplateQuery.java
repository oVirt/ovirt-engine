package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplateQuery<P extends GetVmTemplateParameters> extends QueriesCommandBase<P> {
    public GetVmTemplateQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VmTemplate vmt;
        GetVmTemplateParameters params = getParameters();
        if (params.getName() != null) {
            Guid storagePoolId = getStoragePoolId(); // If no DC info available, the query will return the first
                                                     // Template with the given name found.
            vmt = DbFacade.getInstance().getVmTemplateDao()
                    .getByName(params.getName(), storagePoolId, getUserID(), params.isFiltered());
        }
        else {
            vmt = DbFacade.getInstance().getVmTemplateDao()
                    .get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        }
        if (vmt != null) {
            VmTemplateHandler.updateDisksFromDb(vmt);
            VmHandler.updateVmInitFromDB(vmt, true);
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
            Cluster cluster = DbFacade.getInstance().getClusterDao().get(params.getClusterId());
            if (cluster != null) {
                result = cluster.getStoragePoolId();
            }
        }
        return result;
    }
}
