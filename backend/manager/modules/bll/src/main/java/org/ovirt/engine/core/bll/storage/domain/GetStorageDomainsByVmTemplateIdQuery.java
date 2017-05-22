package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.disk.GetVmTemplatesDisksQuery;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetStorageDomainsByVmTemplateIdQuery<P extends IdQueryParameters>
        extends GetVmTemplatesDisksQuery<P> {

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    private VmTemplate vmTemplate = null;

    public GetStorageDomainsByVmTemplateIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        vmTemplate = vmTemplateDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        ArrayList<StorageDomain> result = new ArrayList<>();

        if (vmTemplate != null && vmTemplate.getStoragePoolId() != null) {
            List<Disk> templateDisks = getTemplateDisks();

            if (templateDisks.size() > 0) {

                Set<Guid> domains = new HashSet<>();
                for (Disk templateDisk : templateDisks) {
                    domains.addAll(((DiskImage)templateDisk).getStorageIds());
                }

                for (Guid domainId : domains) {
                    StorageDomain domain = getStorageDomain(domainId);
                    if (domain != null) {
                        result.add(domain);
                    }
                }
            }
        }
        getQueryReturnValue().setReturnValue(result);
    }

    protected StorageDomain getStorageDomain(Guid domainId) {
        return storageDomainDao.getForStoragePool(domainId, vmTemplate.getStoragePoolId());
    }
}
