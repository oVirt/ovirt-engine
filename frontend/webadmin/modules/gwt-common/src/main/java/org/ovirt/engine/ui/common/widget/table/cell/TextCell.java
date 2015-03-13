package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.widget.table.HasStyleClass;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;

/**
 * A Cell used to render text, providing tooltip in case the content doesn't fit the parent element.
 * <p/>
 * This cell escapes the (String) value when rendering cell HTML.
 */
public class TextCell extends AbstractCellWithTooltip<String> implements HasStyleClass {

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<div class=\"{0}\" style='overflow: hidden; text-overflow: ellipsis; white-space: nowrap;' id=\"{1}\">{2}</div>")
        SafeHtml textContainer(String style, String id, SafeHtml text);
    }

    public static final int UNLIMITED_LENGTH = -1;
    private static final String TOO_LONG_TEXT_POSTFIX = "..."; //$NON-NLS-1$

    private String title;
    private String styleClass = ""; //$NON-NLS-1$

    // Text longer than this value will be shortened, providing tooltip with original text
    private final int maxTextLength;

    private static CellTemplate template;

    public TextCell(int maxTextLength) {
        this(maxTextLength, BrowserEvents.MOUSEOVER);
    }

    public TextCell(int maxTextLength, String... consumedEvents) {
        super(consumedEvents);
        this.maxTextLength = maxTextLength;

        // Delay cell template creation until the first time it's needed
        if (template == null) {
            template = GWT.create(CellTemplate.class);
        }
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass == null ? "" : styleClass; //$NON-NLS-1$
    }

    // This is used to provide an optional tooltip text that is different from the
    // cell value.
    public void setTitle(String value) {
        title = value;
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            SafeHtml escapedValue = getEscapedValue(value);
            SafeHtml renderedValue = getRenderedValue(escapedValue);
            sb.append(template.textContainer(styleClass,
                    id,
                    renderedValue));
        }
    }

    @Override
    protected String getTooltip(String value) {
        // Since tooltips are implemented via HTML 'title' attribute,
        // we must sanitize the value (un-escape and remove HTML tags)
        return new HTML((title != null) ? title : value).getText();
    }

    @Override
    protected boolean showTooltip(Element parent, String value) {
        // Enforce tooltip when the title is explicitly set
        if (title != null) {
            return true;
        }
        // Enforce tooltip when the presented text gets truncated due to maxTextLength
        SafeHtml escapedValue = getEscapedValue(value);
        SafeHtml renderedValue = getRenderedValue(escapedValue);
        return super.showTooltip(parent, value) || !escapedValue.equals(renderedValue);
    }

    SafeHtml getEscapedValue(String value) {
        return SafeHtmlUtils.fromString(value);
    }

    /**
     * Returns the (possibly truncated) value to be rendered by this cell.
     */
    SafeHtml getRenderedValue(SafeHtml value) {
        String result = value.asString();

        // Check if the text needs to be shortened
        if (maxTextLength > 0 && result.length() > maxTextLength) {
            result = result.substring(0, Math.max(maxTextLength - TOO_LONG_TEXT_POSTFIX.length(), 0));
            result = result + TOO_LONG_TEXT_POSTFIX;
        }

        return SafeHtmlUtils.fromSafeConstant(result);
    }

}
