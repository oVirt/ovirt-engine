package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;

/**
 * A Cell used to render text, providing tooltip in case the content doesn't fit the parent element.
 *
 * @see com.google.gwt.cell.client.TextCell
 */
public class TextCellWithTooltip extends AbstractCellWithTooltip<String> {

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<div id=\"{0}\">{1}</div>")
        SafeHtml textContainer(String id, SafeHtml text);

    }

    public static final int UNLIMITED_LENGTH = -1;
    private static final String TOO_LONG_TEXT_POSTFIX = "..."; //$NON-NLS-1$

    // DOM element ID settings for text container element
    private String elementIdPrefix = DOM.createUniqueId();
    private String columnId;
    private String title;

    // Text longer than this value will be shortened, providing tooltip with original text
    private final int maxTextLength;

    private static CellTemplate template;

    public TextCellWithTooltip(int maxTextLength) {
        super("mouseover"); //$NON-NLS-1$
        this.maxTextLength = maxTextLength;

        // Delay cell template creation until the first time it's needed
        if (template == null) {
            template = GWT.create(CellTemplate.class);
        }
    }

    // This is used to provide an optional tooltip text that is different from the
    // cell value.
    public void setTitle(String value) {
        title = value;
    }

    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        if (value != null) {
            SafeHtml escapedValue = getEscapedValue(value);
            SafeHtml renderedValue = getRenderedValue(escapedValue);

            sb.append(template.textContainer(
                    ElementIdUtils.createTableCellElementId(elementIdPrefix, columnId, context),
                    renderedValue));
        }
    }

    @Override
    protected String getTooltip(String value) {
        return getEscapedValue((title != null) ? title : value).asString();
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
