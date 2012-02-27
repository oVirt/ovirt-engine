package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage_pool;
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

    public static boolean checkStoragePool(storage_pool pool, List<String> messages) {
        if (pool == null) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST.toString());
            return false;
        }
        return true;
    }
}
