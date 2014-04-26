package org.ovirt.engine.ui.common.view.popup.numa;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface VNumaTitleTemplate extends SafeHtmlTemplates {
    @Template("<div class=\"{2}\">{0} {1} vNuma</div>")
    SafeHtml title(int count, SafeHtml image, String className);
}
