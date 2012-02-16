package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.GetVmTemplatesDisksQuery;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetStorageDomainsByVmTemplateIdQuery<P extends GetStorageDomainsByVmTemplateIdQueryParameters>
        extends GetVmTemplatesDisksQuery<P> {
    public GetStorageDomainsByVmTemplateIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<storage_domains> result = new java.util.ArrayList<storage_domains>();
        VmTemplate vmTemplate = DbFacade.getInstance().getVmTemplateDAO()
                .get(getParameters().getId());
        if (vmTemplate != null && vmTemplate.getstorage_pool_id() != null) {
            List<DiskImage> templateDisks = getTemplateDisks();

            if (templateDisks.size() > 0) {

                List<Guid> domains =
                        (List<Guid>) DbFacade.getInstance()
                                .getStorageDomainDAO()
                                .getAllStorageDomainsByImageGroup(templateDisks.get(0).getimage_group_id().getValue());

                for (Guid domainId : domains) {
                    storage_domains domain = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(domainId,
                            vmTemplate.getstorage_pool_id().getValue());
                    if (domain != null) {
                        result.add(domain);
                    }
                }
            }
        }
        getQueryReturnValue().setReturnValue(result);
    }
}
