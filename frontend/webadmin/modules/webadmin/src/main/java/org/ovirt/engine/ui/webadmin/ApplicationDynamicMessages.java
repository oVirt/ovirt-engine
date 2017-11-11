package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.ui.common.utils.BaseDynamicMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

public class ApplicationDynamicMessages extends BaseDynamicMessages {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public ApplicationDynamicMessages() {
        super();
        addFallback(DynamicMessageKey.APPLICATION_TITLE, constants.applicationTitle());
        addFallback(DynamicMessageKey.COPY_RIGHT_NOTICE, constants.copyRightNotice());
        addFallback(DynamicMessageKey.DOC, constants.engineWebAdminDoc());
        addFallback(DynamicMessageKey.GUIDE_LINK_LABEL, constants.guideLinkLabel());
        addFallback(DynamicMessageKey.VENDOR_URL, constants.vendorUrl());
    }

}
