package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.utils.ElementUtils;
import org.ovirt.engine.ui.common.widget.table.HasStyleClass;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A Cell used to render text. Supports tooltips. Supports wrapping with a css style. Supports truncation.
 *
 * If truncation is enabled, and if the text doesn't fit in the parent element, it is truncated.
 *
 * There are two types of truncation. You can specify a length in characters, or if you don't, overflow
 * will be detected and truncated via CSS 'text-overflow: ellipsis'.
 *
 * Truncation can also be disabled.
 *
 * When text is truncated, the full text will be rendered in a tooltip, unless a different tooltip is manually
 * configured. Configure a manual tooltip by overriding getTooltip() in the Column using this Cell.
 */
public class TextCell extends AbstractCell<String> implements HasStyleClass {

    public static final int UNLIMITED_LENGTH = -1;
    private static final String ELLIPSE = "..."; //$NON-NLS-1$

    protected interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div class=\"{0}\" style='overflow: hidden; text-overflow: ellipsis; white-space: nowrap;' id=\"{1}\">{2}</div>")
        SafeHtml textContainerWithDetection(String style, String id, SafeHtml text);

        @Template("<div class=\"{0}\" id=\"{1}\">{2}</div>")
        SafeHtml textContainer(String style, String id, SafeHtml text);
    }

    private static CellTemplate template = GWT.create(CellTemplate.class);

    private String styleClass = ""; //$NON-NLS-1$

    // Text longer than this value will be shortened
    private final int maxTextLength;

    // by default, detect overflow and truncate with an ellipse
    private boolean useOverflowTruncation = true;

    public TextCell() {
        this(UNLIMITED_LENGTH, true);
    }

    public TextCell(boolean useOverflowTruncation) {
        this(UNLIMITED_LENGTH, useOverflowTruncation);
    }

    public TextCell(int maxTextLength) {
        this(maxTextLength, false);
    }

    public TextCell(int maxTextLength, boolean useOverflowTruncation) {
        super();
        this.maxTextLength = maxTextLength;
        this.useOverflowTruncation = useOverflowTruncation;
    }

    @Override
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass == null ? "" : styleClass; //$NON-NLS-1$
    }

    public String getStyleClass() {
        return styleClass;
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            SafeHtml safeHtmlValue = SafeHtmlUtils.fromString(value);
            if (maxTextLength >= 0) {
                // using manual truncation
                SafeHtml renderedValue = getRenderedValue(safeHtmlValue);
                sb.append(template.textContainer(getStyleClass(),
                        getRenderElementId(context),
                        renderedValue));
            } else if (useOverflowTruncation) {
                // using overflow truncation
                sb.append(template.textContainerWithDetection(getStyleClass(),
                        getRenderElementId(context),
                        safeHtmlValue));
            } else {
                // no truncation at all
                sb.append(template.textContainer(getStyleClass(),
                        getRenderElementId(context),
                        SafeHtmlUtils.fromString(value)));
            }
        }
    }

    protected String getRenderElementId(Context context) {
        return ElementIdUtils.createTableCellElementId(getElementIdPrefix(), getColumnId(), context);
    }

    @Override
    public SafeHtml getTooltip(String value, Element parent, NativeEvent event) {
        if (value != null) {
            SafeHtml safeHtmlValue = SafeHtmlUtils.fromString(value);
            if (maxTextLength >= 0) {
                SafeHtml renderedValue = getRenderedValue(safeHtmlValue);
                // only render a tooltip if the text actually got truncated
                if (!safeHtmlValue.equals(renderedValue)) {
                    return safeHtmlValue;
                }
            } else if (contentOverflows(parent.getFirstChildElement())) {
                // render a value if there was overflow detected
                return safeHtmlValue;
            }
        }
        return null;
    }

    /**
     * Returns the (possibly truncated) value rendered by this cell.
     */
    private SafeHtml getRenderedValue(SafeHtml value) {
        String result = value.asString();

        // Check if the text needs to be shortened
        if (maxTextLength > 0 && result.length() > maxTextLength) {
            result = result.substring(0, Math.max(maxTextLength - ELLIPSE.length(), 0));
            result = result + ELLIPSE;
        }

        return SafeHtmlUtils.fromTrustedString(result);
    }

    /**
     * Returns {@code true} when the content of the given {@code parent} element overflows its area.
     */
    protected boolean contentOverflows(Element parent) {
        return parent != null && (ElementUtils.detectHorizontalOverflow(parent) || ElementUtils.detectVerticalOverflow(parent));
    }

}
