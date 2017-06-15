package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;

import com.google.inject.Inject;

/**
 * Provides static access to resources, constants, templates, and messages.
 */
public class AssetProvider {

    @Inject
    static CommonApplicationConstants commonApplicationConstantsProvider;

    public static CommonApplicationConstants getConstants() {
        return commonApplicationConstantsProvider;
    }

    @Inject
    static CommonApplicationMessages commonApplicationMessagesProvider;

    public static CommonApplicationMessages getMessages() {
        return commonApplicationMessagesProvider;
    }

    @Inject
    static CommonApplicationTemplates commonApplicationTemplatesProvider;

    public static CommonApplicationTemplates getTemplates() {
        return commonApplicationTemplatesProvider;
    }

    @Inject
    static CommonApplicationResources commonApplicationResourcesProvider;

    public static CommonApplicationResources getResources() {
        return commonApplicationResourcesProvider;
    }

}
