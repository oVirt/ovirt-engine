package org.ovirt.engine.ui.common.view.popup.numa;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface NumaTitleTemplate extends SafeHtmlTemplates {
    @Template("<div class=\"{1}\">{0} NUMA</div>")
    SafeHtml title(int count, String className);
}
