package org.ovirt.engine.core.vdsbroker.storage;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class StorageDomainHelper {

    /**
     * If the storage domain given in the parameter is a block domain, the number of LVs on this domain will be fetched
     * and if it exceeds the maximum number of LVs defined in the AlertOnNumberOfLVs config value, an audit log will
     * be logged to indicate that the number of LVs on this domain exceeded the allowed number of LVs
     */
    public static void checkNumberOfLVsForBlockDomain(Guid storageDomainId) {
        StorageDomainStatic domain = DbFacade.getInstance().getStorageDomainStaticDao().get(storageDomainId);
        if (domain.getStorageType().isBlockDomain()) {
            long numOfLVs = DbFacade.getInstance().getStorageDomainDao().getNumberOfImagesInStorageDomain(storageDomainId);
            Integer maxNumOfLVs = Config.getValue(ConfigValues.AlertOnNumberOfLVs);
            if (numOfLVs >= maxNumOfLVs) {
                AuditLogableBase logable = new AuditLogableBase();
                logable.addCustomValue("storageDomainName", domain.getStorageName());
                logable.addCustomValue("maxNumOfLVs", maxNumOfLVs.toString());
                logable.setStorageDomainId(storageDomainId);
                new AuditLogDirector().log(logable, AuditLogType.NUMBER_OF_LVS_ON_STORAGE_DOMAIN_EXCEEDED_THRESHOLD);
            }
        }
    }
}
