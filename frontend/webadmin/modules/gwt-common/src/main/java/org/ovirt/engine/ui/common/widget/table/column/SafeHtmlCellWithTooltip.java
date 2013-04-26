package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class SafeHtmlCellWithTooltip extends AbstractCellWithTooltip<SafeHtml> {

    public SafeHtmlCellWithTooltip() {
        super("mouseover"); //$NON-NLS-1$
    }

    public SafeHtmlCellWithTooltip(String... consumedEvents) {
        super(consumedEvents);
    }

    @Override
    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.appendHtmlConstant("<div style='display:block'>"); //$NON-NLS-1$
            sb.append(value);
            sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
        }
    }

    @Override
    protected boolean contentOverflows(Element parent) {
        // Perform content overflow detection on child DIV element
        return super.contentOverflows(parent.getFirstChildElement());
    }

    @Override
    protected String getTooltip(SafeHtml value) {
        return value.asString();
    }

}
