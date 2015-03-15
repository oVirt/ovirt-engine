package org.ovirt.engine.core.bll.storage;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageHelperDirector {
    private static final String ACTION_TYPE_PACKAGE = "org.ovirt.engine.core.bll.storage";
    private static final String ACTION_TYPE_CLASS = "StorageHelper";

    private static final Logger log = LoggerFactory.getLogger(StorageHelperDirector.class);

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
                        log.debug("StorageHelperDirector Error:: the lookup for following class has failed: {}",
                                formattedClassName);
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
            log.error("StorageHelperDirector Error:: exception was encountered during InitializeHelpers() execution: {}",
                    ex.getMessage());
            log.debug("Exception");
            throw new RuntimeException(ex);
        }
    }

    public IStorageHelper getItem(StorageType index) {
        return _helpers.get(index);
    }
}
