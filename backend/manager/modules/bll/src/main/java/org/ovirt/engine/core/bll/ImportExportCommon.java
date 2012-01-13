package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class ImportExportCommon {
    // check that storage domain (by id) exists
    public static boolean CheckStorageDomain(Guid storageDomainId, java.util.ArrayList<String> messages) {
        boolean returnValue = DbFacade.getInstance().getStorageDomainStaticDAO().get(storageDomainId) != null;
        if (!returnValue) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST.toString());
        }
        return returnValue;
    }

    public static boolean CheckStoragePool(Guid storagePoolId, java.util.ArrayList<String> messages) {
        boolean returnValue = DbFacade.getInstance().getStorageDomainStaticDAO().getAllForStoragePool(storagePoolId) != null;
        if (!returnValue) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST.toString());
        }
        return returnValue;
    }
}
