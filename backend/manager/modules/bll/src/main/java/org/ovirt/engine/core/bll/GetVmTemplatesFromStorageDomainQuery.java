package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.comparators.VmTemplateComparerByDiskSize;
import org.ovirt.engine.core.common.queries.GetVmTemplatesFromStorageDomainParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesFromStorageDomainQuery<P extends GetVmTemplatesFromStorageDomainParameters>
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
        if (getParameters().isWithDisks()) {
            for (VmTemplate template : returnValue) {
                VmTemplateHandler.updateDisksFromDb(template);
                Collections.sort(template.getDiskList(), new DiskByDiskAliasComparator());

            }
        }
        Collections.sort(returnValue, Collections.reverseOrder(new VmTemplateComparerByDiskSize()));
        getQueryReturnValue().setReturnValue(returnValue);
    }
}
