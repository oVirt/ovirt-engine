package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;

/**
 * A Cell used to render HTML, providing tooltip in case the content doesn't fit the parent element.
 * <p/>
 * This cell does not escape the (SafeHtml) value when rendering cell HTML, i.e. the value is considered to be 'safe'
 * already.
 */
public class SafeHtmlCell extends AbstractCellWithTooltip<SafeHtml> {

    public SafeHtmlCell() {
        super(BrowserEvents.MOUSEOVER);
    }

    public SafeHtmlCell(String... consumedEvents) {
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
    protected String getTooltip(SafeHtml value) {
        // Since tooltips are implemented via HTML 'title' attribute,
        // we must sanitize the value (un-escape and remove HTML tags)
        return new HTML(value.asString()).getText();
    }

}
