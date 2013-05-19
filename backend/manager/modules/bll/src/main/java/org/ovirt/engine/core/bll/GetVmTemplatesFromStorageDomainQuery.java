package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.DiskImageByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.comparators.VmTemplateComparerByDiskSize;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesFromStorageDomainQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetVmTemplatesFromStorageDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> returnValue = DbFacade.getInstance()
                .getVmTemplateDao()
                        .getAllForStorageDomain(getParameters().getId(),
                                getUserID(),
                                getParameters().isFiltered());
        for (VmTemplate template : returnValue) {
            VmTemplateHandler.UpdateDisksFromDb(template);
            java.util.Collections.sort(template.getDiskList(), new DiskImageByDiskAliasComparator());

        }
        Collections.sort(returnValue, Collections.reverseOrder(new VmTemplateComparerByDiskSize()));
        getQueryReturnValue().setReturnValue(returnValue);
    }
}
