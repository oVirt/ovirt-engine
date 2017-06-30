package org.ovirt.engine.core.vdsbroker.storage;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;

@Singleton
public class StorageDomainHelper {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    /**
     * If the storage domain given in the parameter is a block domain, the number of LVs on this domain will be fetched
     * and if it exceeds the maximum number of LVs defined in the AlertOnNumberOfLVs config value, an audit log will
     * be logged to indicate that the number of LVs on this domain exceeded the allowed number of LVs
     */
    public void checkNumberOfLVsForBlockDomain(Guid storageDomainId) {
        StorageDomainStatic domain = storageDomainStaticDao.get(storageDomainId);
        if (domain.getStorageType().isBlockDomain()) {
            long numOfLVs = storageDomainDao.getNumberOfImagesInStorageDomain(storageDomainId);
            Integer maxNumOfLVs = Config.getValue(ConfigValues.AlertOnNumberOfLVs);
            if (numOfLVs >= maxNumOfLVs) {
                AuditLogable logable = new AuditLogableImpl();
                logable.setStorageDomainName(domain.getName());
                logable.addCustomValue("maxNumOfLVs", maxNumOfLVs.toString());
                logable.setStorageDomainId(storageDomainId);
                auditLogDirector.log(logable, AuditLogType.NUMBER_OF_LVS_ON_STORAGE_DOMAIN_EXCEEDED_THRESHOLD);
            }
        }
    }
}
