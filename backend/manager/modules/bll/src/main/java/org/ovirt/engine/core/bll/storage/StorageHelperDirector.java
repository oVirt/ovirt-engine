package org.ovirt.engine.core.bll.storage;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class StorageHelperDirector {
    private static final String ACTION_TYPE_PACKAGE = "org.ovirt.engine.core.bll.storage";
    private static final String ACTION_TYPE_CLASS = "StorageHelper";

    private static final Log log = LogFactory.getLog(StorageHelperDirector.class);

    private static StorageHelperDirector _instance = new StorageHelperDirector();
    private Map<StorageType, IStorageHelper> _helpers =
            new EnumMap<StorageType, IStorageHelper>(StorageType.class);

    public static StorageHelperDirector getInstance() {
        return _instance;
    }

    private StorageHelperDirector() {
        InitializeHelpers();
    }

    private void InitializeHelpers() {
        try {
            for (StorageType storageType : StorageType.values()) {
                if (storageType.isConcreteStorageType()) {
                    Class<?> actionType = null;
                    String formattedClassName = String.format("%1$s.%2$s%3$s",
                            ACTION_TYPE_PACKAGE,
                            storageType.name(),
                            ACTION_TYPE_CLASS);
                    try {
                        actionType = Class.forName(formattedClassName);
                    } catch (ClassNotFoundException cnfe) {
                        log.debugFormat("StorageHelperDirector Error:: the lookup for following class has failed: {0}"
                                , formattedClassName);
                    }

                    // if action type not exist - operation invalid
                    if (actionType != null) {
                        Constructor<?> info = actionType.getConstructors()[0];
                        IStorageHelper currentHelper = (IStorageHelper) info.newInstance(null);
                        _helpers.put(storageType, currentHelper);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("StorageHelperDirector Error:: exception was encountered during InitializeHelpers() execution",
                    ex);
            throw new RuntimeException(ex);
        }
    }

    public IStorageHelper getItem(StorageType index) {
        return _helpers.get(index);
    }
}
