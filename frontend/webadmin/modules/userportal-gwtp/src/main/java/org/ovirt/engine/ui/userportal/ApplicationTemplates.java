package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;

import com.google.gwt.safehtml.shared.SafeHtml;

public interface ApplicationTemplates extends CommonApplicationTemplates {

    @Template("<span style='font-size: 14px; font-family: Arial,sans-serif; font-weight: bold;'>{0}</span>")
    SafeHtml vmNameCellItem(String name);

    @Template(" ({0})")
    SafeHtml vmDescriptionCellItem(String description);

}
