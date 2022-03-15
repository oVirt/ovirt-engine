package org.ovirt.engine.ui.uicommonweb.models.options;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.WebAdminSettings;
import org.ovirt.engine.ui.uicommonweb.dataprovider.LocalStorage;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

class LocalStoragePersistenceField implements Field<Boolean> {
    private final EntityModel<Boolean> localStoragePersistedOnServer;
    private boolean originalLocalStoragePersistedOnServer;
    private final LocalStorage localStorage;
    private final Boolean defaultValue;
    private final boolean resettable;

    public LocalStoragePersistenceField(EntityModel<Boolean> model,
            LocalStorage localStorage,
            boolean resettable) {
        localStoragePersistedOnServer = model;
        this.localStorage = localStorage;
        defaultValue = model.getEntity();
        this.resettable = resettable;
        originalLocalStoragePersistedOnServer = defaultValue;
    }

    @Override
    public EntityModel<Boolean> getEntity() {
        return localStoragePersistedOnServer;
    }

    @Override
    public boolean isUpdated() {
        return !Objects.equals(originalLocalStoragePersistedOnServer,
                localStoragePersistedOnServer.getEntity());
    }

    @Override
    public UserProfileProperty toProp() {
        Map<String, String> storage = Collections.emptyMap();
        if (localStoragePersistedOnServer.getEntity()) {
            storage = localStorage.getAllSupportedMappingsFromLocalStorage();
        }
        return WebAdminSettings.Builder.create()
                .fromSettings(Frontend.getInstance().getWebAdminSettings())
                .withLocalStoragePersistence(localStoragePersistedOnServer.getEntity())
                // clear the storage on the server when persistence gets disabled
                // upload local state to the server otherwise
                .withStorage(storage)
                .build()
                .encode();
    }

    @Override
    public void fromProp(UserProfileProperty prop) {
        boolean flag = WebAdminSettings.from(prop).isLocalStoragePersistedOnServer();
        originalLocalStoragePersistedOnServer = flag;
        localStoragePersistedOnServer.setEntity(flag);
    }

    @Override
    public boolean isSupported(UserProfileProperty prop) {
        return WebAdminSettings.WEB_ADMIN.equals(prop.getName());
    }

    @Override
    public boolean isResettable() {
        return resettable;
    }

    @Override
    public boolean isCustom() {
        return !Objects.equals(defaultValue, originalLocalStoragePersistedOnServer);
    }
}
