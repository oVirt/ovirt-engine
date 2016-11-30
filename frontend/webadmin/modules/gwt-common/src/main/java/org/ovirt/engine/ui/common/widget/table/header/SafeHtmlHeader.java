package org.ovirt.engine.ui.common.widget.table.header;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlCell;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A Header that renders SafeHtml. Supports tooltips. Supports element-id framework.
 *
 * @param <H> Cell data type.
 */
public class SafeHtmlHeader extends AbstractHeader<SafeHtml> implements ColumnWithElementId, TooltipHeader {

    private SafeHtml headerText;
    private SafeHtml renderedHeaderText;
    private String tooltipText;

    public static final SafeHtmlHeader BLANK_HEADER = new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant("")); //$NON-NLS-1$

    public SafeHtmlHeader(SafeHtmlCell safeHtmlCell) {
        this(null, null, safeHtmlCell);
    }

    public SafeHtmlHeader(SafeHtml headerText) {
        this(headerText, createSafeHtmlCell());
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtmlCell safeHtmlCell) {
        this(headerText, null, safeHtmlCell);
    }

    public SafeHtmlHeader(SafeHtml headerText, String tooltipText) {
        this(headerText, tooltipText, createSafeHtmlCell());
    }

    public SafeHtmlHeader(SafeHtml headerText, String tooltipText, SafeHtmlCell safeHtmlCell) {
        super(safeHtmlCell);
        setValue(headerText);
        setTooltip(tooltipText);
    }

    public static SafeHtmlCell createSafeHtmlCell() {
        return new SafeHtmlCell() {
            @Override
            public Set<String> getConsumedEvents() {
                Set<String> set = new HashSet<>(super.getConsumedEvents());
                set.add(BrowserEvents.CLICK); // for sorting
                return set;
            }
        };
    }

    public static SafeHtmlHeader fromSafeConstant(String headerText) {
        return new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(headerText));
    }

    public SafeHtmlCell getCell() {
        return (SafeHtmlCell) super.getCell();
    }

    @Override
    public String getTooltip() {
        return tooltipText;
    }

    /**
     * Return the SafeHtml to be rendered.
     * @see com.google.gwt.user.cellview.client.Header#getValue()
     */
    @Override
    public SafeHtml getValue() {
        return renderedHeaderText;
    }

    protected void setValue(SafeHtml headerText) {
        this.headerText = headerText;
        setHeaderTooltipStyle(this.tooltipText);
    }

    protected void setTooltip(String tooltipText) {
        this.tooltipText = tooltipText;
        setHeaderTooltipStyle(tooltipText);
    }

    /**
     * Toggle the header tooltip style (to give a visual clue that this header can be hovered over).
     */
    protected void setHeaderTooltipStyle(String tooltipText) {
        if (tooltipText == null || tooltipText.isEmpty()) {
            renderedHeaderText = this.headerText;
        }
        else {
            renderedHeaderText = templates.hasTooltip(headerText);
        }
    }

}
