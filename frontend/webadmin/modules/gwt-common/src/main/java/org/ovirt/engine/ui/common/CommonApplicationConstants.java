package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Constants;

public interface CommonApplicationConstants extends Constants {

    @DefaultStringValue("Oops!")
    String errorPopupCaption();

    @DefaultStringValue("Close")
    String closeButtonLabel();

    @DefaultStringValue("[N/A]")
    String unAvailablePropertyLabel();

    // Widgets

    @DefaultStringValue("Next >>")
    String actionTableNextPageButtonLabel();

    @DefaultStringValue("<< Prev")
    String actionTablePrevPageButtonLabel();

    // Table columns

    @DefaultStringValue("Disk Activate/Deactivate while VM is running, is supported only for Clusters of version 3.1 and above")
    String diskHotPlugNotSupported();

}
