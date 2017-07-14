package org.ovirt.engine.core.bll.storage.connection;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

@Singleton
public class StorageHelperDirector {
    private Map<StorageType, IStorageHelper> helpers = new EnumMap<>(StorageType.class);

    @Inject
    private Instance<IStorageHelper> injectedHelpers;

    @PostConstruct
    public void initializeHelpers() {
        for (IStorageHelper helper : injectedHelpers) {
            for (StorageType type : helper.getTypes()) {
                helpers.put(type, helper);
            }
        }
    }

    public IStorageHelper getItem(StorageType index) {
        return helpers.get(index);
    }
}
