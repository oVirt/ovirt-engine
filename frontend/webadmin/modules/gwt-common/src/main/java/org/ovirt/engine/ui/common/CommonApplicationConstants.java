package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Constants;

public interface CommonApplicationConstants extends Constants {

    @DefaultStringValue("Oops!")
    String errorPopupCaption();

    @DefaultStringValue("Close")
    String closeButtonLabel();

    @DefaultStringValue("[N/A]")
    String unAvailablePropertyLabel();

    @DefaultStringValue("This feature is not implemented in this version.")
    String featureNotImplementedMessage();

    @DefaultStringValue("This feature is not implemented but available in UserPortal for users assigned with PowerUser role.")
    String featureNotImplementedButAvailInUserPortalMessage();

}
