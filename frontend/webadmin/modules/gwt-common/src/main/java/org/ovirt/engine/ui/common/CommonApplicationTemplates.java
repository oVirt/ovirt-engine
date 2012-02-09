package org.ovirt.engine.ui.common;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface CommonApplicationTemplates extends SafeHtmlTemplates {

    @Template("<span><span style='position: relative; vertical-align: middle;'>{0}</span>" +
            "<span style='position: relative; margin-left: 3px; white-space: nowrap;'>{1}</span></span>")
    SafeHtml imageTextButton(SafeHtml image, String text);

    @Template("<span><span style='position: relative; vertical-align: middle;'>{0}</span>" +
            "<span style='position: relative; margin-left: 3px; white-space: nowrap;'>{1}</span></span>")
    SafeHtml textImageButton(String text, SafeHtml image);

}
