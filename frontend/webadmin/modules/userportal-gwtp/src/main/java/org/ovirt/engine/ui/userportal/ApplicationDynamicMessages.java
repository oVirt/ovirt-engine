package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.utils.BaseDynamicMessages;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;

public class ApplicationDynamicMessages extends BaseDynamicMessages {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public ApplicationDynamicMessages() {
        super();
        addFallback(DynamicMessageKey.APPLICATION_TITLE, constants.applicationTitle());
        addFallback(DynamicMessageKey.VERSION_ABOUT, constants.ovirtVersionAbout());
        addFallback(DynamicMessageKey.COPY_RIGHT_NOTICE, constants.copyRightNotice());
        addFallback(DynamicMessageKey.DOC, constants.userPortalDoc());
        addFallback(DynamicMessageKey.GUIDE_LINK_LABEL, constants.guideLinkLabel());
        addFallback(DynamicMessageKey.VENDOR_URL, constants.vendorUrl());
    }

    /**
     * Get the extended guide URL using the {@code Dictionary} in the host page. Uses current locale (e.g. "en_US")
     * for placeholder {0}, if it exists. With a fall back to the standard GWT Constant.
     *
     * @return The guide URL.
     */
    public final String extendedGuideUrl() {
        return formatString(DynamicMessageKey.EXTENDED_GUIDE_URL, getCurrentLocaleAsString());
    }

}
