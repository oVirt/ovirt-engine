package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesByStoragePoolIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetVmTemplatesByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        StoragePool pool = DbFacade.getInstance().getStoragePoolDao().get(getParameters().getId());
        SearchParameters p = new SearchParameters(String.format("Templates: DataCenter = %1$s", pool.getname()),
                SearchType.VmTemplate);
        p.setMaxCount(Integer.MAX_VALUE);
        VdcQueryReturnValue returnValue = Backend.getInstance().runInternalQuery(VdcQueryType.Search, p);

        if (returnValue != null && returnValue.getSucceeded()) {
            List<VmTemplate> templateList = (List) returnValue.getReturnValue();
            VmTemplate blank = DbFacade.getInstance().getVmTemplateDao()
                    .get(VmTemplateHandler.BlankVmTemplateId);
            if (!templateList.contains(blank)) {
                templateList.add(0, blank);
            }
            getQueryReturnValue().setReturnValue(templateList);
        }
    }
}
