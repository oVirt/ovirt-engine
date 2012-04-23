package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplatesByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesByStoragePoolIdQuery<P extends GetVmTemplatesByStoragePoolIdParameters>
        extends QueriesCommandBase<P> {
    public GetVmTemplatesByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        storage_pool pool = DbFacade.getInstance().getStoragePoolDAO().get(getParameters().getStoragePoolId());
        SearchParameters p = new SearchParameters(String.format("Templates: DataCenter = %1$s", pool.getname()),
                SearchType.VmTemplate);
        p.setMaxCount(Integer.MAX_VALUE);
        VdcQueryReturnValue returnValue = Backend.getInstance().runInternalQuery(VdcQueryType.Search, p);

        if (returnValue != null && returnValue.getSucceeded()) {
            List<VmTemplate> templateList = (List) returnValue.getReturnValue();
            VmTemplate blank = DbFacade.getInstance().getVmTemplateDAO()
                    .get(VmTemplateHandler.BlankVmTemplateId);
            if (!templateList.contains(blank)) {
                templateList.add(0, blank);
            }
            getQueryReturnValue().setReturnValue(templateList);
        }
    }
}
