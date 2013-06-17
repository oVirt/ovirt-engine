package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.utils.DynamicMessages;

import com.google.gwt.core.client.GWT;

public class ApplicationDynamicMessages extends DynamicMessages {

    private static final ApplicationConstants constants = GWT.create(ApplicationConstants.class);

    public ApplicationDynamicMessages() {
        super();
        addFallback(DynamicMessageKey.APPLICATION_TITLE, constants.applicationTitle());
        addFallback(DynamicMessageKey.VERSION_ABOUT, constants.ovirtVersionAbout());
        addFallback(DynamicMessageKey.LOGIN_HEADER_LABEL, constants.loginHeaderLabel());
        addFallback(DynamicMessageKey.MAIN_HEADER_LABEL, constants.mainHeaderLabel());
        addFallback(DynamicMessageKey.COPY_RIGHT_NOTICE, constants.copyRightNotice());
        addFallback(DynamicMessageKey.DOC, constants.userPortalDoc());
    }

}
