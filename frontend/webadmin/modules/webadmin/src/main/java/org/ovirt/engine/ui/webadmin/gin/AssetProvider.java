package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.inject.Inject;

/**
 * Provides static access to resources, constants, templates, and messages.
 *
 */
public class AssetProvider {

    @Inject
    static ApplicationConstants applicationConstantsProvider;

    public static ApplicationConstants getConstants() {
        return applicationConstantsProvider;
    }

    @Inject
    static ApplicationMessages applicationMessagesProvider;

    public static ApplicationMessages getMessages() {
        return applicationMessagesProvider;
    }

    @Inject
    static ApplicationTemplates applicationTemplatesProvider;

    public static ApplicationTemplates getTemplates() {
        return applicationTemplatesProvider;
    }

    @Inject
    static ApplicationResources applicationResourcesProvider;

    public static ApplicationResources getResources() {
        return applicationResourcesProvider;
    }

}
