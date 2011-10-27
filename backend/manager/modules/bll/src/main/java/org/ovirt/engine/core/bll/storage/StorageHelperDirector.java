package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class StorageHelperDirector {
    private static StorageHelperDirector _instance = new StorageHelperDirector();
    private java.util.HashMap<StorageType, IStorageHelper> _helpers =
            new java.util.HashMap<StorageType, IStorageHelper>();

    public static StorageHelperDirector getInstance() {
        return _instance;
    }

    private StorageHelperDirector() {
        InitializeHelpers();
    }

    private void InitializeHelpers() {
        try {
            for (String helperName : EnumCompat.GetNames(StorageType.class)) {
                java.lang.Class actionType = null;
                try {
                    actionType = java.lang.Class.forName(String.format("%1$s.%2$s%3$s", "org.ovirt.engine.core.bll.storage",
                            helperName, "StorageHelper"));
                } catch (ClassNotFoundException cnfe) {
                    // eat it
                }
                /**
                 * if action type not exist - operation valid
                 */
                if (actionType != null) {
                    java.lang.reflect.Constructor info = actionType.getConstructors()[0];
                    Object tempVar = info.newInstance(null);
                    IStorageHelper currentHelper = (IStorageHelper) ((tempVar instanceof IStorageHelper) ? tempVar
                            : null);
                    if (currentHelper != null) {
                        _helpers.put(StorageType.valueOf(helperName), currentHelper);
                    }
                }
            }
        } catch (Exception ex) {
            throw new ApplicationException("JTODO missing exception", ex);
        }
    }

    public IStorageHelper getItem(StorageType index) {
        return _helpers.get(index);
    }
}
