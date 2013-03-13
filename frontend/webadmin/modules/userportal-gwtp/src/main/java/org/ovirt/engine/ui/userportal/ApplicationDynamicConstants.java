package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.utils.DynamicConstants;

import com.google.gwt.core.client.GWT;

public class ApplicationDynamicConstants extends DynamicConstants {

    private static final ApplicationConstants constants = GWT.create(ApplicationConstants.class);

    public ApplicationDynamicConstants() {
        super();
        addFallback(DynamicConstantKey.APPLICATION_TITLE, constants.applicationTitle());
        addFallback(DynamicConstantKey.VERSION_ABOUT, constants.ovirtVersionAbout());
        addFallback(DynamicConstantKey.LOGIN_HEADER_LABEL, constants.loginHeaderLabel());
        addFallback(DynamicConstantKey.MAIN_HEADER_LABEL, constants.mainHeaderLabel());
        addFallback(DynamicConstantKey.COPY_RIGHT_NOTICE, constants.copyRightNotice());
    }

}
