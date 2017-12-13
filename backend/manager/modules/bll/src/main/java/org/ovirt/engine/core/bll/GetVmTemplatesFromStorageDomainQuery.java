package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.queries.GetVmTemplatesFromStorageDomainParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetVmTemplatesFromStorageDomainQuery<P extends GetVmTemplatesFromStorageDomainParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmTemplateHandler vmTemplateHandler;

    public GetVmTemplatesFromStorageDomainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> returnValue =
                vmTemplateDao.getAllForStorageDomain(getParameters().getId(),
                                getUserID(),
                                getParameters().isFiltered());
        if (getParameters().isWithDisks()) {
            for (VmTemplate template : returnValue) {
                vmTemplateHandler.updateDisksFromDb(template);
                Collections.sort(template.getDiskList(), new DiskByDiskAliasComparator());

            }
        }
        Collections.sort(returnValue, Comparator.comparing(VmTemplate::getActualDiskSize).reversed());
        getQueryReturnValue().setReturnValue(returnValue);
    }
}
