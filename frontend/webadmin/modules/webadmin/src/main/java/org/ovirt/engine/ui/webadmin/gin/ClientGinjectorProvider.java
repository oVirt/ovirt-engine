package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provides static access to common dependencies.
 *
 * @deprecated This class is meant to support existing code that used to access {@code ClientGinjector} directly. New
 *             code should <b>not</b> use this class, always {@code @Inject} specific dependencies directly.
 */
@Deprecated
public class ClientGinjectorProvider {

    @Inject
    static Provider<EventBus> eventBusProvider;

    public static EventBus getEventBus() {
        return eventBusProvider.get();
    }

    @Inject
    static Provider<ApplicationConstants> applicationConstantsProvider;

    public static ApplicationConstants getApplicationConstants() {
        return applicationConstantsProvider.get();
    }

    @Inject
    static Provider<ApplicationMessages> applicationMessagesProvider;

    public static ApplicationMessages getApplicationMessages() {
        return applicationMessagesProvider.get();
    }

    @Inject
    static Provider<ApplicationTemplates> applicationTemplatesProvider;

    public static ApplicationTemplates getApplicationTemplates() {
        return applicationTemplatesProvider.get();
    }

    @Inject
    static Provider<ApplicationResources> applicationResourcesProvider;

    public static ApplicationResources getApplicationResources() {
        return applicationResourcesProvider.get();
    }

    @Inject
    static Provider<ClientStorage> clientStorageProvider;

    public static ClientStorage getClientStorage() {
        return clientStorageProvider.get();
    }

}
